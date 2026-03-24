package com.pebble.feature.chat.di

import com.pebble.feature.chat.ui.ChatViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    // aiClient and chatRepository are satisfied by :core:ai and :core:data modules respectively.
    viewModel { ChatViewModel(aiClient = get(), chatRepository = get()) }
}
