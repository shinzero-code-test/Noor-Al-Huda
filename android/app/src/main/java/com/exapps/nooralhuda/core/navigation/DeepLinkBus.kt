package com.exapps.nooralhuda.core.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Carries VIEW-intent links from MainActivity to the nav-scoped AuthViewModel. */
@Singleton
class DeepLinkBus @Inject constructor() {
    private val _links = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val links: SharedFlow<String> = _links.asSharedFlow()

    fun emit(link: String) {
        _links.tryEmit(link)
    }
}
