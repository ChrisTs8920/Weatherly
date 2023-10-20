package com.example.weatherly

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherly.ui.theme.WeatherlyTheme
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
            WeatherlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var input by remember {
        mutableStateOf("Athens")
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

    var dataEffect by remember {
        mutableStateOf(false)
    }

    //var showShimmer by remember {
        //mutableStateOf(true)
    //}

    //var showContent by remember {
        //mutableStateOf(0F)
    //}

    LaunchedEffect(dataEffect) {
        //showShimmer = true
        //showContent = 0F
        prevData = homeData
        prevForecastData = forecastData

        homeData = WeatherApi.readMainData(input)
        forecastData = WeatherApi.readForecastData(input)

        if (homeData == WeatherApi.dummyData()) { // In case of readData failure(i.e provided City does not exist), keep previous data
            homeData = prevData
            forecastData = prevForecastData
            input = prevInput
        } else {
            prevInput = input
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
        //showShimmer = false
        //showContent = 255F
    }

    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopBar(homeData)
        },
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) {
            innerPadding -> NavHost(navController, Screen.Main.route, Modifier.padding(innerPadding)) {
                composable(Screen.Main.route) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally) {

                        Spacer(modifier = Modifier.height(30.dp))
                        InputText(input = input, onValueChange = { input = it }) {
                            dataEffect = !dataEffect
                        }
                        Spacer(modifier = Modifier.height(105.dp))
                        PrimaryStats(data = homeData, currIcon = currIcon)
                        Spacer(modifier = Modifier.height(105.dp))
                        SecondaryStats(
                            data = homeData,
                            formatter = formatter,
                            sunriseZone = sunriseZone,
                            sunsetZone = sunsetZone,
                            updatedOnTime = updatedOnTime
                        )
                    }
                }
                composable(Screen.Forecast.route) {
                    Forecast(forecastData)
                }
                composable(Screen.Settings.route) {
                    Settings()
                }
        }
    }
}

@Composable
fun Forecast(data: WeatherApi.ForecastJsonData) {
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
        .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.height(150.dp))
        Text("5 Day forecast", color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(10.dp))
        for (j in 0..< icons.size) {
            ForecastItem(data, icons[j], j * 8, dayString[j])
        }
    }
}

@Composable
fun ForecastItem(data: WeatherApi.ForecastJsonData, icon: Int, it: Int, day: String) {
    Surface(shape = MaterialTheme.shapes.small,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = icon),
                contentDescription = "Weather")
            Spacer(modifier = Modifier.width(10.dp))
            Text(data.list[it].weather[0].main, modifier = Modifier.weight(0.4F))
            Spacer(modifier = Modifier.width(40.dp))
            Text(day, modifier = Modifier.weight(0.4F))
            Spacer(modifier = Modifier.width(80.dp))
            Text(data.list[it].main.temp.roundToInt().toString() + "°C", modifier = Modifier.weight(0.2F))
        }
    }
}

@Composable
fun Settings() {
    Text("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(data: WeatherApi.HomeJsonData) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = data.name + ", " + data.sys.country,
                //modifier = Modifier.background(shimmerBrush(targetValue = 1300f, showShimmer = showShimmer)).alpha(showContent),
                style = MaterialTheme.typography.titleLarge)
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val items = listOf(Screen.Main, Screen.Forecast, Screen.Settings)
    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { screen ->
            NavigationBarItem(selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = { navController.navigate(screen.route) }, icon = { Icon(
                imageVector = screen.icon,
                contentDescription = "Home")})
        }
    }
    /*BottomAppBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.9F)) {
            Text(
                text = "Weatherly",
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp))
            Text(
                text = "Data provided by OpenWeatherMap.org",
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                style = MaterialTheme.typography.bodySmall)
        }
    }*/
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputText(input: String, onValueChange: (String) -> Unit, onDone: (Unit) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
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
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = MaterialTheme.colorScheme.surface,
            textColor = MaterialTheme.colorScheme.onSurface,
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
fun PrimaryStats(data: WeatherApi.HomeJsonData, currIcon: Int) {
    Text(data.main.temp.roundToInt().toString() + "°C",
        //modifier = Modifier.background(shimmerBrush(targetValue = 1300f, showShimmer = showShimmer)).alpha(showContent),
        style = TextStyle(
            fontSize = MaterialTheme.typography.displayLarge.fontSize,
            brush = Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary))),
        color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically)/*modifier = Modifier.padding(5.dp).background(shimmerBrush(targetValue = 1300f, showShimmer = showShimmer)).alpha(showContent)*/ {
        Image(painter = painterResource(id = currIcon),
            contentDescription = "Weather")
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = data.weather[0].main, color = MaterialTheme.colorScheme.onSurface)
    }
    Spacer(modifier = Modifier.height(10.dp))
    Row/*(modifier = Modifier.padding(5.dp).background(shimmerBrush(targetValue = 1300f, showShimmer = showShimmer)).alpha(showContent))*/ {
        Icon(imageVector = Icons.Default.KeyboardArrowDown,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "min")
        Text(text = data.main.tempMin.roundToInt().toString() + "°C",
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(10.dp))
        Icon(imageVector = Icons.Default.KeyboardArrowUp,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "max")
        Text(text = data.main.tempMax.roundToInt().toString() + "°C",
            color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SecondaryStats(data: WeatherApi.HomeJsonData,
                   formatter: DateTimeFormatter,
                   sunriseZone: ZonedDateTime,
                   sunsetZone: ZonedDateTime,
                   updatedOnTime: ZonedDateTime) {
    Row {
        Surface(shape = MaterialTheme.shapes.small,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .weight(0.5F)
                .padding(5.dp)) {
            Row/*(modifier = Modifier.background(shimmerBrush(targetValue = 1300f, showShimmer = showShimmer)).alpha(showContent))*/ {
                Text(text = "Real Feel:\nHumidity:\nPressure:",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyMedium)
                Text(text = data.main.feelsLike.roundToInt().toString() + "°\n"
                        + data.main.humidity + "%\n"
                        + data.main.pressure + "hPa", modifier = Modifier
                    .padding(10.dp)
                    .weight(0.3F),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        Surface(shape = MaterialTheme.shapes.small,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .weight(0.5F)
                .padding(5.dp)) {
            Row/*(modifier = Modifier.background(shimmerBrush(targetValue = 1300f, showShimmer = showShimmer)).alpha(showContent))*/ {
                Text(text = "Wind Speed:\nWind Dir:\nVisibility:",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyMedium)
                Text(text = data.wind.speed.toString() + "m/s\n"
                        + data.wind.deg + "°\n"
                        + (data.visibility / 1000) + "km", modifier = Modifier
                    .padding(10.dp)
                    .weight(0.3F),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    Row {
        Surface(shape = MaterialTheme.shapes.small,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .weight(0.5F)
                .padding(5.dp)) {
            Row/*(modifier = Modifier.background(shimmerBrush(targetValue = 1300f, showShimmer = showShimmer)).alpha(showContent))*/ {
                Text(text = "Sunrise:\nSunset:\nUpdated on:",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyMedium)
                Text(text = formatter.format(sunriseZone) + "\n"
                        + formatter.format(sunsetZone) + "\n"
                        + formatter.format(updatedOnTime),
                    modifier = Modifier
                        .padding(10.dp)
                        .weight(0.3F),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        Surface(shape = MaterialTheme.shapes.small,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .weight(0.5F)
                .padding(5.dp)) {
            Row/*(modifier = Modifier.background(shimmerBrush(targetValue = 1300f, showShimmer = showShimmer)).alpha(showContent))*/ {
                Text(text = "Sea level:\nGround level:\nCloudiness:",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyMedium)
                Text(text = data.main.seaLevel.toString() + "hPa\n"
                        + data.main.grndLevel + "hPa\n"
                        + data.clouds.all + "%", modifier = Modifier
                    .padding(10.dp)
                    .weight(0.3F),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/*@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue:Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent,Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}*/

@Preview(showBackground = true, name = "Dark theme", uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true, name = "Light theme", uiMode = UI_MODE_NIGHT_NO)
@Composable
fun AppPreview() {
    WeatherlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            App()
        }
    }
}

@Preview
@Composable
fun ForecastItemPreview() {
    WeatherlyTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            ForecastItem(data = WeatherApi.forecastDummyData(),
                icon = R.drawable._01d,
                it = 0,
                "Friday")
        }
    }
}