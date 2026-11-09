package br.com.cacapalavras.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import br.com.cacapalavras.ui.screens.CharacterScreen
import br.com.cacapalavras.ui.screens.PassScreen
import br.com.cacapalavras.ui.screens.ShopScreen
import br.com.cacapalavras.ui.screens.StreakScreen
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cacapalavras.ui.screens.AchievementsScreen
import br.com.cacapalavras.ui.screens.GameScreen
import br.com.cacapalavras.ui.screens.HomeScreen
import br.com.cacapalavras.ui.screens.LevelsScreen
import br.com.cacapalavras.ui.screens.ProfileScreen
import br.com.cacapalavras.ui.screens.ResultScreen
import br.com.cacapalavras.ui.screens.ThemesScreen
import br.com.cacapalavras.ui.theme.CacaTheme
import br.com.cacapalavras.ui.theme.Palettes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            App(vm, onPaletteDark = { dark ->
                val style = if (dark) SystemBarStyle.dark(0) else SystemBarStyle.light(0, android.graphics.Color.BLACK)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            })
        }
    }
}

@Composable
private fun App(vm: AppViewModel, onPaletteDark: (Boolean) -> Unit) {
    val loaded by vm.progress.collectAsStateWithLifecycle()
    val progress = loaded ?: return // aguarda o DataStore (uma fração de segundo)
    val palette = Palettes.byId(progress.selectedPalette)
    LaunchedEffect(palette.dark) { onPaletteDark(palette.dark) }
    LaunchedEffect(vm.toast) {
        if (vm.toast != null) {
            delay(2200)
            vm.toast = null
        }
    }

    CacaTheme(palette) {
        Box(Modifier.fillMaxSize().background(palette.background)) {
            BackHandler(enabled = vm.backStack.size > 1 && vm.screen != Screen.Game) { vm.back() }
            AnimatedContent(
                targetState = vm.screen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                contentKey = { it::class },
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                label = "screens",
            ) { screen ->
                when (screen) {
                    Screen.Home -> HomeScreen(
                        progress = progress,
                        dailyDone = vm.isDailyDone(progress),
                        onJourney = { vm.startJourney() },
                        onLevels = { vm.navigate(Screen.Levels) },
                        onDaily = { vm.startDaily() },
                        onTimeAttack = { vm.startTimeAttack() },
                        onAchievements = { vm.navigate(Screen.Achievements) },
                        onThemes = { vm.navigate(Screen.Themes) },
                        onProfile = { vm.navigate(Screen.Profile) },
                        onCharacter = { vm.navigate(Screen.Character) },
                        onPass = { vm.navigate(Screen.Pass) },
                        onShop = { vm.navigate(Screen.Shop) },
                        onStreak = { vm.navigate(Screen.Streak) },
                        onDismissNotif = { vm.dismissNotificationPrompt() },
                    )
                    Screen.Levels -> LevelsScreen(progress, onBack = vm::back, onPlay = { vm.startJourney(it) })
                    Screen.Game -> vm.game?.let { GameScreen(it, progress, onQuit = vm::back) }
                    is Screen.Result -> ResultScreen(
                        result = screen,
                        progress = progress,
                        hapticsOn = progress.hapticsEnabled,
                        onNext = { (screen.request as? GameRequest.Journey)?.let { vm.startJourney(it.level + 1) } },
                        onReplay = { vm.startTimeAttack() },
                        onHome = vm::home,
                    )
                    Screen.Achievements -> AchievementsScreen(progress, onBack = vm::back)
                    Screen.Themes -> ThemesScreen(progress, onBack = vm::back, onSelect = { vm.selectPalette(it) })
                    Screen.Profile -> ProfileScreen(
                        progress, onBack = vm::back,
                        onHaptics = { vm.setHaptics(it) },
                        onReset = { vm.resetProgress(); vm.home() },
                        onName = { vm.setPlayerName(it) },
                        onCharacter = { vm.navigate(Screen.Character) },
                    )
                    Screen.Character -> CharacterScreen(
                        progress, onBack = vm::back,
                        onEquip = { vm.equip(it) }, onUnequip = { vm.unequip(it) }, onInfo = { vm.toast = it },
                    )
                    Screen.Pass -> PassScreen(
                        progress, onBack = vm::back,
                        onClaim = { t, prem -> vm.claim(t, prem) }, onClaimAll = { vm.claimAll() },
                        onBuyPremium = { vm.buyPremium() }, onShop = { vm.navigate(Screen.Shop) },
                        onTestXp = { vm.testAddXp() }, onTestMinds = { vm.testAddMinds() }, onTestReset = { vm.testResetPass() },
                    )
                    Screen.Shop -> ShopScreen(progress, onBack = vm::back, onBuy = { vm.testPurchase(it) })
                    Screen.Streak -> StreakScreen(
                        progress, onBack = vm::back,
                        onReminder = { vm.setReminderEnabled(it) }, onHour = { vm.setReminderHour(it) },
                        onLastChance = { vm.setLastChance(it) }, onTest = { vm.testNotification() },
                    )
                }
            }
            // Aviso curto
            AnimatedVisibility(
                visible = vm.toast != null,
                modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
                enter = fadeIn() + slideInVertically { it }, exit = fadeOut(),
            ) {
                Text(
                    vm.toast ?: "", color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xEE222844)).padding(horizontal = 18.dp, vertical = 12.dp),
                )
            }
        }
    }
}
