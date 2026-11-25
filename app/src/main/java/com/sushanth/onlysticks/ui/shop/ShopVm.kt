// ViewModel for the shop tab.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.data.repository.CardStoreState
import com.sushanth.onlysticks.data.repository.PackBuy
import com.sushanth.onlysticks.data.repository.ShopRepository
import com.sushanth.onlysticks.domain.gacha.PackType
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.store.BuyRes
import com.sushanth.onlysticks.ui.common.UiTxt
import com.sushanth.onlysticks.ui.common.uiTxt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tabs in the shop. */
enum class ShopTab { PACKS, CARDS }

/** Shop state. [shown] holds cards currently being revealed. */
data class ShopUiState(
    val tab: ShopTab = ShopTab.PACKS,
    val cardStore: CardStoreState? = null,
    val shown: List<Card>? = null,
    val message: UiTxt? = null,
    val busy: Boolean = false
)

/** Handles purchases and keeps the weekly store up to date by re-checking the store window every [refrMs]. */
@OptIn(ExperimentalCoroutinesApi::class)
class ShopVm(private val shop: ShopRepository, refrMs: Long = 30_000L) : ViewModel() {
    private val _state = MutableStateFlow(ShopUiState())
    val state: StateFlow<ShopUiState> = _state.asStateFlow()

    /** Polls the store window; when it changes (opens, closes or refreshes) switches to the new offers. */
    init {
        viewModelScope.launch {
            flow { while (true) { emit(shop.curWin()); delay(refrMs) } }
                .distinctUntilChanged()
                .flatMapLatest(shop::observeCardStore)
                .collect { store -> _state.update { it.copy(cardStore = store) } }
        }
    }

    /** Switches tab. */
    fun selectTab(tab: ShopTab) = _state.update { it.copy(tab = tab) }

    /** Buys and opens a pack. */
    fun buyPack(type: PackType) = buy {
        when (val result = shop.buyPack(type)) {
            is PackBuy.Opened -> _state.update { it.copy(shown = result.result.cardList) }
            PackBuy.NotEnoughCoins -> _state.update { it.copy(message = uiTxt(R.string.shop_not_enough)) }
        }
    }

    /** Buys the store card in [slot]. */
    fun buyCard(slot: Int) = buy {
        val result = shop.buyCard(slot)
        _state.update {
            when (result) {
                is BuyRes.Success -> it.copy(shown = listOf(result.card))
                BuyRes.NotEnoughCoins -> it.copy(message = uiTxt(R.string.shop_not_enough))
                BuyRes.SoldOut -> it.copy(message = uiTxt(R.string.shop_sold_out))
                BuyRes.StoreClosed -> it.copy(message = uiTxt(R.string.shop_closed))
            }
        }
    }

    /** Closes the reveal overlay. */
    fun dismissShow() = _state.update { it.copy(shown = null) }

    /** Runs a purchase in the background, ignoring taps while one is in progress. */
    private fun buy(block: suspend () -> Unit) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try { block() } finally { _state.update { it.copy(busy = false) } }
        }
    }
}
