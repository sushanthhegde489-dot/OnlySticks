// ViewModel for the admin screen.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.data.repository.AdminRepository
import com.sushanth.onlysticks.data.repository.NotAdminException
import com.sushanth.onlysticks.domain.gacha.PackRes
import com.sushanth.onlysticks.domain.gacha.PackType
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.ui.common.UiTxt
import com.sushanth.onlysticks.ui.common.uiTxt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tabs on the admin screen. */
enum class AdminTab { ECONOMY, CREATE, COLLECTION }

/** Admin screen state. [opened] holds the result of a free pack so it can be shown. */
data class AdminUiState(
    val tab: AdminTab = AdminTab.ECONOMY,
    val busy: Boolean = false,
    val message: UiTxt? = null,
    val opened: PackRes? = null,
)

/** Forwards admin actions to [AdminRepository] and reports success or failure as a message. */
class AdminVm(private val admin: AdminRepository) : ViewModel() {
    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    /** Switches tab. */
    fun selectTab(tab: AdminTab) = _state.update { it.copy(tab = tab) }

    /** Adds (or removes, if negative) coins. */
    fun adjCoins(amount: Long) = run {
        val bal = admin.adjCoins(amount)
        say(uiTxt(R.string.admin_msg_bal, bal))
    }

    /** Opens the weekly store for 24 hours. */
    fun forceStoreRefr() = run {
        admin.forceStoreRefr()
        say(uiTxt(R.string.admin_msg_store))
    }

    /** Opens a pack without paying. */
    fun openFreePack(type: PackType) = run {
        val result = admin.openFreePack(type)
        _state.update { it.copy(opened = result) }
    }

    /** Creates a stick; random stats when [stats] is null. */
    fun createStick(rarity: Rarity, stats: BaseStats?, tuning: Set<TuningFlag>) = run {
        admin.createStick(rarity, stats, tuning)
        say(uiTxt(R.string.admin_msg_created))
    }

    /** Creates a power-up. */
    fun createPwrUp(type: PwrUpType) = run {
        admin.createPwrUp(type)
        say(uiTxt(R.string.admin_msg_created))
    }

    /** Creates a trainer. */
    fun createTrnr(buff: TrnrBuff, affinity: Int) = run {
        admin.createTrnr(buff, affinity)
        say(uiTxt(R.string.admin_msg_created))
    }

    /** Saves edits to a card. */
    fun save(card: Card) = run {
        admin.updateCard(card)
        say(uiTxt(R.string.admin_msg_saved))
    }

    /** Saves edits to a trainer, including its win count. */
    fun saveTrnr(trainer: com.sushanth.onlysticks.domain.model.Trnr, wins: Int) = run {
        admin.updateCard(trainer)
        admin.setTrnrProg(trainer.id, trainer.affinity, wins)
        say(uiTxt(R.string.admin_msg_saved))
    }

    /** Deletes a card. */
    fun delete(card: Card) = run {
        admin.deleteCard(card)
        say(uiTxt(R.string.admin_msg_deleted))
    }

    /** Closes the pack reveal. */
    fun dismissPack() = _state.update { it.copy(opened = null) }

    /** Shows a status message. */
    private fun say(text: UiTxt) = _state.update { it.copy(message = text) }

    /** Runs an admin action in the background with a busy flag; errors become a message. */
    private fun run(block: suspend () -> Unit) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: NotAdminException) {
                say(uiTxt(R.string.admin_msg_denied))
            } catch (e: Exception) {
                say(uiTxt(R.string.acct_err))
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }
}
