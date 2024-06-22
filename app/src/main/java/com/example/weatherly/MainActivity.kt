package com.example.weatherly

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherly.ui.theme.WeatherlyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // Adds Splash Screen, Configure in res/values/splash.xml
        enableEdgeToEdge() // Adds color to status bar and navigation bar
        super.onCreate(savedInstanceState)
        setContent {
            val dataStore = DataStore(LocalContext.current)
            val darkModeState by dataStore.darkModeFlow.collectAsState(initial = isSystemInDarkTheme())
            val cityState by dataStore.cityFlow.collectAsState(initial = "Athens")
            WeatherlyTheme(useDarkTheme = darkModeState) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(dataStore, darkModeState, cityState)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App(dataStore: DataStore, darkModeState: Boolean, citySate: String) {
    var input by remember {
        mutableStateOf("")
    }

    var prevInput by remember {
        mutableStateOf(input)
    }

    var homeData by remember {
        mutableStateOf(WeatherApi.dummyData())
    }

    var prevData by remember {
        mutableStateOf(homeData)
    }

    var forecastData by remember {
        mutableStateOf(WeatherApi.forecastDummyData())
    }

    var prevForecastData by remember {
        mutableStateOf(WeatherApi.forecastDummyData())
    }

    val formatter by remember {
        mutableStateOf(DateTimeFormatter.ofPattern("HH:mm"))
    }

    // Set Sunset, Sunrise and updatedOn to appropriate format and time zone.
    var sunriseTime by remember {
        mutableStateOf(ZonedDateTime.ofInstant(Instant.ofEpochSecond(homeData.sys.sunrise.toLong()), ZoneId.of("UTC")))
    }

    var sunsetTime by remember {
        mutableStateOf(ZonedDateTime.ofInstant(Instant.ofEpochSecond(homeData.sys.sunset.toLong()), ZoneId.of("UTC")))
    }

    var updatedOnTime by remember {
        mutableStateOf(Instant.ofEpochSecond(homeData.dt.toLong()).atZone(ZoneId.systemDefault()))
    }

    var sunriseZone by remember {
        mutableStateOf(sunriseTime.withZoneSameInstant(ZoneOffset.ofHours(homeData.timezone / 3600)))
    }

    var sunsetZone by remember {
        mutableStateOf(sunsetTime.withZoneSameInstant(ZoneOffset.ofHours(homeData.timezone / 3600)))
    }

    var currIcon by remember {
        mutableIntStateOf(R.drawable._01d)
    }

    var atSettings by remember {
        mutableStateOf(false)
    }

    val navController = rememberNavController()

    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    fun refresh() = refreshScope.launch {
        refreshing = true
        delay(1000)
        refreshing = false
    }
    val state = rememberPullRefreshState(refreshing, ::refresh)

    LaunchedEffect(citySate) {
        prevData = homeData
        prevForecastData = forecastData

        homeData = WeatherApi.readMainData(citySate)
        forecastData = WeatherApi.readForecastData(citySate)

        if (homeData == WeatherApi.dummyData()) { // In case of readData failure(i.e provided City does not exist), keep previous data
            homeData = prevData
            forecastData = prevForecastData
            dataStore.writeCity(prevInput)
        } else {
            prevInput = citySate
        }

        // Update time Zones
        updatedOnTime = Instant.ofEpochSecond(homeData.dt.toLong()).atZone(ZoneId.systemDefault())
        sunriseTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(homeData.sys.sunrise.toLong()), ZoneId.of("UTC"))
        sunsetTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(homeData.sys.sunset.toLong()), ZoneId.of("UTC"))
        sunriseZone = sunriseTime.withZoneSameInstant(ZoneOffset.ofHours(homeData.timezone / 3600))
        sunsetZone = sunsetTime.withZoneSameInstant(ZoneOffset.ofHours(homeData.timezone / 3600))

        // Update weather Icon
        when (homeData.weather[0].icon) {
            "01d" -> currIcon = R.drawable._01d
            "01n" -> currIcon = R.drawable._01d
            "02d" -> currIcon = R.drawable._02d
            "02n" -> currIcon = R.drawable._02d
            "03d" -> currIcon = R.drawable._03d
            "03n" -> currIcon = R.drawable._03d
            "04d" -> currIcon = R.drawable._04d
            "04n" -> currIcon = R.drawable._04d
            "09d" -> currIcon = R.drawable._09d
            "09n" -> currIcon = R.drawable._09d
            "10d" -> currIcon = R.drawable._10d
            "10n" -> currIcon = R.drawable._10d
            "11d" -> currIcon = R.drawable._11d
            "11n" -> currIcon = R.drawable._11d
            "13d" -> currIcon = R.drawable._13d
            "13n" -> currIcon = R.drawable._13d
            "50d" -> currIcon = R.drawable._50d
            "50n" -> currIcon = R.drawable._50d
        }
    }

    Scaffold(
        topBar = {
            TopBar(homeData, atSettings)
        },
        bottomBar = {
            BottomBar(navController) {
                atSettings = navController.currentDestination?.route == Screen.Settings.route
            }
        }
    ) {
        innerPadding -> NavHost(navController, Screen.Main.route, Modifier.padding(innerPadding)) {
            composable(Screen.Main.route) {
                AnimatedContent(targetState = homeData != WeatherApi.dummyData(), label = "Anim",
                    transitionSpec = { fadeIn() togetherWith fadeOut()}) {
                    targetState ->
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .fillMaxSize()
                            .pullRefresh(state)
                            .paint(
                                painterResource(id = R.drawable.cloud),
                                alignment = BiasAlignment(-1.7f, -0.35f),
                                colorFilter = ColorFilter.tint(
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        4.dp
                                    )
                                )
                            )
                            .paint(
                                painterResource(id = R.drawable.cloud),
                                alignment = BiasAlignment(1.7f, 0.3f),
                                colorFilter = ColorFilter.tint(
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        4.dp
                                    )
                                )
                            )
                            .paint(
                                painterResource(id = R.drawable.cloud),
                                alignment = BiasAlignment(1.7f, -1f),
                                colorFilter = ColorFilter.tint(
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        4.dp
                                    )
                                )
                            )
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (targetState) {
                            PullRefreshIndicator(refreshing = refreshing, state = state,
                                Modifier.align(Alignment.CenterHorizontally).zIndex(1f),
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.height(20.dp))
                            InputText(input = input, onValueChange = { input = it }) {
                                runBlocking {
                                    dataStore.writeCity(input)
                                    input = ""
                                }
                            }
                            Spacer(modifier = Modifier.height(40.dp))
                            PrimaryStats(data = homeData,
                                currIcon = currIcon,
                                data2 = forecastData,
                                formatter = formatter,
                                sunriseZone = sunriseZone,
                                sunsetZone = sunsetZone)
                            Spacer(modifier = Modifier.height(120.dp))
                            SecondaryStats(
                                data = homeData,
                                formatter = formatter,
                                updatedOnTime = updatedOnTime)
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.width(64.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
            composable(Screen.Forecast.route) {
                AnimatedContent(targetState = homeData != WeatherApi.dummyData(), label = "Anim2",
                    transitionSpec = { fadeIn() togetherWith fadeOut()}) { targetState ->
                    if (targetState) {
                        Forecast(forecastData, state, refreshing)
                    } else {
                        Column(modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(64.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
            composable(Screen.Settings.route) {
                Settings(darkModeState, dataStore)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Forecast(data: WeatherApi.ForecastJsonData, state: PullRefreshState, refreshing: Boolean) {
    val icons by remember {
        mutableStateOf(mutableListOf(R.drawable._01d, R.drawable._01d, R.drawable._01d, R.drawable._01d, R.drawable._01d))
    }

    val dayString by remember {
        mutableStateOf(mutableListOf("Friday", "Friday", "Friday", "Friday", "Friday"))
    }

    LaunchedEffect(Unit) {
        for ((i, j) in (0..< icons.size).withIndex()) {
            // Setup days
            val day = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data.list[i * 8].dt.toLong()), ZoneOffset.UTC).dayOfWeek
            dayString[j] = day.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
            // Update weather Icons
            when (data.list[i * 8].weather[0].icon) {
                "01d" -> icons[j] = R.drawable._01d
                "01n" -> icons[j] = R.drawable._01d
                "02d" -> icons[j] = R.drawable._02d
                "02n" -> icons[j] = R.drawable._02d
                "03d" -> icons[j] = R.drawable._03d
                "03n" -> icons[j] = R.drawable._03d
                "04d" -> icons[j] = R.drawable._04d
                "04n" -> icons[j] = R.drawable._04d
                "09d" -> icons[j] = R.drawable._09d
                "09n" -> icons[j] = R.drawable._09d
                "10d" -> icons[j] = R.drawable._10d
                "10n" -> icons[j] = R.drawable._10d
                "11d" -> icons[j] = R.drawable._11d
                "11n" -> icons[j] = R.drawable._11d
                "13d" -> icons[j] = R.drawable._13d
                "13n" -> icons[j] = R.drawable._13d
                "50d" -> icons[j] = R.drawable._50d
                "50n" -> icons[j] = R.drawable._50d
            }
        }
    }

    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .fillMaxSize().pullRefresh(state)
        .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {

        PullRefreshIndicator(refreshing = refreshing, state = state,
            Modifier.align(Alignment.CenterHorizontally).zIndex(1f),
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary)
        Spacer(modifier = Modifier.weight(1f))
        Text("5 Day forecast", color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(10.dp))
        for (j in 0..< icons.size) {
            ForecastItem(data, icons[j], j * 8, dayString[j], j)
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ForecastItem(data: WeatherApi.ForecastJsonData, icon: Int, it: Int, day: String, j: Int) {
    Surface(shape = MaterialTheme.shapes.large,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation((j + 1).dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .height(140.dp)) {
        Column(verticalArrangement = Arrangement.SpaceEvenly) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                Image(painter = painterResource(id = icon),
                    contentDescription = "Weather", modifier = Modifier.size(70.dp))
                Text(day, fontSize = MaterialTheme.typography.titleLarge.fontSize, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(20.dp))
                Text(data.list[it].main.temp.roundToInt().toString() + "°C",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                Image(painterResource(id = R.drawable.thermometer), contentDescription = "real feel",
                    modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = data.list[it].main.feelsLike.roundToInt().toString() + "°", color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(20.dp))
                Image(painterResource(id = R.drawable.humidity), contentDescription = "humidity",
                    modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = data.list[it].main.humidity.toString() + "%", color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(20.dp))
                Image(painterResource(id = R.drawable.compass), contentDescription = "wind dir",
                    modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = data.list[it].wind.deg.toString() + "°", color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(20.dp))
                Image(painterResource(id = R.drawable.speed), contentDescription = "wind speed",
                    modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = data.list[it].wind.speed.toInt().toString() + "m/s", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun Settings(checked: Boolean, dataStore: DataStore) {
    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.height(30.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark Mode", modifier = Modifier
                .weight(0.5F)
                .padding(start = 40.dp),
                style = MaterialTheme.typography.bodyLarge)
            Switch(checked = checked, onCheckedChange = { runBlocking { dataStore.writeTheme(it) } },
                modifier = Modifier
                    .weight(0.5F)
                    .padding(start = 40.dp),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.tertiaryContainer,
                    checkedThumbColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        Spacer(modifier = Modifier.height(30.dp))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Weatherly",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = "Data provided by OpenWeatherMap.org",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(data: WeatherApi.HomeJsonData, atSettings: Boolean) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = if (atSettings) "Settings" else data.name,
                style = MaterialTheme.typography.titleLarge)
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun BottomBar(navController: NavController, checkScreen: (Unit) -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val items = listOf(Screen.Main, Screen.Forecast, Screen.Settings)
    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { screen ->
            NavigationBarItem(selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = { navController.navigate(screen.route)
                    checkScreen(Unit)}, icon = { Icon(
                imageVector = screen.icon,
                contentDescription = "Home")})
        }
    }
}

@Composable
fun InputText(input: String, onValueChange: (String) -> Unit, onDone: (Unit) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(shape = MaterialTheme.shapes.large,
        value = input,
        label = { Text(text = "Search a city..")},
        onValueChange = { onValueChange(it) },
        keyboardActions = KeyboardActions(onDone = {
            if (input.isNotEmpty()) {
                onDone(Unit)
            }
            focusManager.clearFocus()
        }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor =  MaterialTheme.colorScheme.tertiary,
            unfocusedTextColor = MaterialTheme.colorScheme.tertiary,
            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            focusedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer),
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onTertiaryContainer) })
}

@Composable
fun PrimaryStats(data: WeatherApi.HomeJsonData,
                 currIcon: Int,
                 data2: WeatherApi.ForecastJsonData,
                 formatter: DateTimeFormatter,
                 sunriseZone: ZonedDateTime,
                 sunsetZone: ZonedDateTime) {
    val day = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data2.list[0].dt.toLong()), ZoneOffset.UTC).dayOfWeek
    val dayString = day.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = currIcon),
            contentDescription = "Weather",
            modifier = Modifier.size(70.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = "Today", fontSize = MaterialTheme.typography.displaySmall.fontSize)
            Text(text = dayString, fontSize = MaterialTheme.typography.titleMedium.fontSize)
        }
    }
    Spacer(modifier = Modifier.height(40.dp))
    Row {
        Spacer(modifier = Modifier.width(30.dp))
        Text(data.main.temp.roundToInt().toString(),
            style = TextStyle(
                fontSize = 70.sp,
                brush = Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary))),
            color = MaterialTheme.colorScheme.primary)
        Text("°C", style = TextStyle(
            fontSize = 30.sp,
            brush = Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary))),
            color = MaterialTheme.colorScheme.primary)
    }
    Spacer(modifier = Modifier.height(10.dp))
    Row {
        Text(text = data.weather[0].main, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = data.main.tempMin.roundToInt().toString() + "°",
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(5.dp))
        Text("|", color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = data.main.tempMax.roundToInt().toString() + "°",
            color = MaterialTheme.colorScheme.onSurface)
    }
    Spacer(modifier = Modifier.height(10.dp))
    Row {
        Text(text = "Sunrise " + formatter.format(sunriseZone), color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "•", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Sunset " + formatter.format(sunsetZone), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SecondaryStats(data: WeatherApi.HomeJsonData,
                   formatter: DateTimeFormatter,
                   updatedOnTime: ZonedDateTime) {
    Row {
        Surface(shape = MaterialTheme.shapes.large,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(0.3F)
                .padding(5.dp)) {
            Column(modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(id = R.drawable.thermometer), contentDescription = "thermometer",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Feels like " + data.main.feelsLike.roundToInt().toString() + "°", color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Surface(shape = MaterialTheme.shapes.large,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(0.3F)
                .padding(5.dp)) {
            Column(modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(id = R.drawable.compass), contentDescription = "thermometer",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Wind direction " + data.wind.deg + "°", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
    Row {
        Surface(shape = MaterialTheme.shapes.large,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(0.3F)
                .padding(5.dp)) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(id = R.drawable.humidity), contentDescription = "humidity",
                        modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                    Text(text = "Humidity",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(text = data.main.humidity.toString() + "%",
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(0.3F),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(
                    modifier = Modifier.padding(5.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(id = R.drawable.speed), contentDescription = "speed",
                        modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                    Text(text = "Wind speed",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(text = data.wind.speed.toInt().toString() + "m/s",
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(0.3F),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(
                    modifier = Modifier.padding(5.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(id = R.drawable.pressure), contentDescription = "pressure",
                        modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                    Text(text = "Pressure",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(text = data.main.pressure.toString() + "hPa",
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(0.3F),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(
                    modifier = Modifier.padding(5.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(id = R.drawable.visibility), contentDescription = "visibility",
                        modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                    Text(text = "Visibility",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(text = (data.visibility / 1000).toString() + "km",
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(0.3F),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(
                    modifier = Modifier.padding(5.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(id = R.drawable.updated), contentDescription = "updated on",
                        modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                    Text(text = "Updated on",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(text =  formatter.format(updatedOnTime),
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(0.3F),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }

        }
    }
}

@Preview(showBackground = true, name = "Dark theme", uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true, name = "Light theme", uiMode = UI_MODE_NIGHT_NO)
@Composable
fun AppPreview() {
    val dataStore = DataStore(LocalContext.current)
    WeatherlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            App(dataStore, true, "Athens")
        }
    }
}

//@OptIn(ExperimentalMaterialApi::class)
//@Preview(showBackground = true, name = "Dark theme", uiMode = UI_MODE_NIGHT_YES)
//@Composable
//fun ForecastPreview() {
//    WeatherlyTheme {
//        Surface(
//            color = MaterialTheme.colorScheme.background
//        ) {
//            Forecast(data = WeatherApi.forecastDummyData())
//        }
//    }
//}