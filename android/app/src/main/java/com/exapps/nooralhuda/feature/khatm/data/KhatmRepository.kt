package com.exapps.nooralhuda.feature.khatm.data

import com.exapps.nooralhuda.core.privacy.PrivacyManager
import com.exapps.nooralhuda.feature.khatm.domain.KhatmGroup
import com.exapps.nooralhuda.feature.khatm.domain.KhatmMember
import com.exapps.nooralhuda.feature.khatm.domain.KhatmRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed group khatm, shaped exactly by the v2 rules contract:
 * invite-code joins, owned member slots, client-computed progress (no
 * shared counters). Privacy mode disables everything (no listeners, no
 * writes) — khatm is a cloud feature by nature.
 */
@Singleton
class FirestoreKhatmRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val privacy: PrivacyManager
) : KhatmRepository {

    private fun uid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("Sign in required")

    private fun displayName(): String =
        auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Noor member"

    private fun groups() = firestore.collection("khatm_groups")
    private fun members() = firestore.collection("khatm_members")
    private fun invites() = firestore.collection("khatm_invites")

    private fun QuerySnapshot.toMembers(): List<KhatmMember> = documents.mapNotNull { doc ->
        val groupId = doc.getString("groupId") ?: return@mapNotNull null
        val memberUid = doc.getString("uid") ?: return@mapNotNull null
        @Suppress("UNCHECKED_CAST")
        val pages = (doc.get("completedPages") as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }?.toSet() ?: emptySet()
        KhatmMember(
            groupId = groupId,
            uid = memberUid,
            displayName = doc.getString("displayName") ?: "Noor member",
            completedPages = pages
        )
    }

    private fun DocumentSnapshot.toGroup(code: String? = null): KhatmGroup? {
        val name = getString("name") ?: return null
        return KhatmGroup(
            id = id,
            name = name,
            openJoin = getBoolean("openJoin") == true,
            createdBy = getString("created_by") ?: "",
            inviteCode = code
        )
    }

    override fun myGroups(): Flow<List<KhatmGroup>> {
        if (auth.currentUser == null) return emptyFlow()
        return callbackFlow {
            if (!privacy.canFetchRemote()) {
                close()
                return@callbackFlow
            }
            // Own slots first (owner-readable), then each group doc
            // (member-gated gets — fail-closed per group via runCatching).
            val registration = members().whereEqualTo("uid", auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, _ ->
                    val ids = snapshot?.toMembers()?.map { it.groupId }?.toSet() ?: emptySet()
                    launch {
                        val resolved = ids.mapNotNull { id ->
                            runCatching { groups().document(id).get().await().toGroup() }
                                .getOrNull()
                        }
                        trySend(resolved)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    override fun openGroups(): Flow<List<KhatmGroup>> = callbackFlow {
        if (!privacy.canFetchRemote() || auth.currentUser == null) {
            close()
            return@callbackFlow
        }
        // Discovery query MUST filter openJoin == true (rules deny otherwise).
        val registration = groups().whereEqualTo("openJoin", true)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.mapNotNull { it.toGroup() } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    override fun members(groupId: String): Flow<List<KhatmMember>> = callbackFlow {
        if (!privacy.canFetchRemote() || auth.currentUser == null) {
            close()
            return@callbackFlow
        }
        val registration = members().whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toMembers() ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createGroup(name: String, openJoin: Boolean): Result<Unit> =
        runCatching {
            val me = uid()
            if (!privacy.canFetchRemote()) throw IllegalStateException("Offline")
            val code = if (openJoin) null else newInviteCode()
            val ref = groups().add(
                mapOf(
                    "name" to name.take(80),
                    "created_by" to me,
                    "openJoin" to openJoin,
                    "createdAt" to FieldValue.serverTimestamp()
                ) + (code?.let { mapOf("inviteCode" to it) } ?: emptyMap())
            ).await()
            // Slot yourself (creator-add path covers the closed case).
            members().document("${ref.id}__$me").set(
                mapOf(
                    "groupId" to ref.id,
                    "uid" to me,
                    "displayName" to displayName(),
                    "completedPages" to emptyList<Int>(),
                    "joinedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            // Closed groups get a fresh invite code (mirrored on the group
            // doc so the creator can re-read it; the code itself is the
            // secret and invite docs stay unlistable).
            if (!openJoin && code != null) {
                invites().document(code).set(
                    mapOf("groupId" to ref.id, "createdAt" to FieldValue.serverTimestamp())
                ).await()
            }
            Unit
        }

    override suspend fun joinWithCode(code: String): Result<KhatmGroup> = runCatching {
        val me = uid()
        if (!privacy.canFetchRemote()) throw IllegalStateException("Offline")
        val normalized = code.trim().uppercase()
        val invite = invites().document(normalized).get().await()
        val groupId = invite.getString("groupId")
            ?: throw IllegalStateException("Unknown invite code")
        members().document("${groupId}__$me").set(
            mapOf(
                "groupId" to groupId,
                "uid" to me,
                "displayName" to displayName(),
                "completedPages" to emptyList<Int>(),
                "joinedAt" to FieldValue.serverTimestamp(),
                "inviteCode" to normalized
            )
        ).await()
        groups().document(groupId).get().await().toGroup(normalized)
            ?: throw IllegalStateException("Group unavailable")
    }

    override suspend fun joinOpen(groupId: String): Result<Unit> = runCatching {
        val me = uid()
        if (!privacy.canFetchRemote()) throw IllegalStateException("Offline")
        members().document("${groupId}__$me").set(
            mapOf(
                "groupId" to groupId,
                "uid" to me,
                "displayName" to displayName(),
                "completedPages" to emptyList<Int>(),
                "joinedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Unit
    }

    override suspend fun markPageDone(groupId: String, page: Int): Result<Unit> = runCatching {
        val me = uid()
        if (!privacy.canFetchRemote()) throw IllegalStateException("Offline")
        require(page in 1..604) { "page out of range" }
        members().document("${groupId}__$me")
            .update("completedPages", FieldValue.arrayUnion(page)).await()
        Unit
    }

    override suspend fun leave(groupId: String): Result<Unit> = runCatching {
        val me = uid()
        members().document("${groupId}__$me").delete().await()
        Unit
    }

    override suspend fun removeMember(groupId: String, uid: String): Result<Unit> = runCatching {
        uid()
        members().document("${groupId}__$uid").delete().await()
        Unit
    }

    /** Invite code for a group you created (null when open or not yours). */
    override suspend fun inviteCodeFor(groupId: String): String? {
        if (!privacy.canFetchRemote()) return null
        val me = auth.currentUser?.uid ?: return null
        val group = runCatching { groups().document(groupId).get().await() }.getOrNull()
            ?: return null
        if (group.getString("created_by") != me) return null
        if (group.getBoolean("openJoin") == true) return null
        return group.getString("inviteCode")
    }

    private fun newInviteCode(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val random = SecureRandom()
        return (1..8).map { alphabet[random.nextInt(alphabet.length)] }.joinToString("")
    }
}
