package com.example.weatherly

class AQI(
    private var pm10: Double,
    //private var so2 = 0.0
    private var pm2_5: Double,
    private var o3: Double,
    //private var co = 0.0
    //private var no = 0.0
    private var no2: Double
) {

    data class AQIBreakpoint(val cLow: Double, val cHigh: Double, val iLow: Int, val iHigh: Int)

    // Source: OpenWeatherMap.org - Europe based indexes
    private val pm25Breakpoints = listOf(
        AQIBreakpoint(0.0, 15.0, 0, 25),
        AQIBreakpoint(15.0, 30.0, 25, 50),
        AQIBreakpoint(30.0, 55.0, 50, 75),
        AQIBreakpoint(55.0, 110.0, 75, 100),
        AQIBreakpoint(110.0, 300.0, 100, 200)
    )

    private val pm10Breakpoints = listOf(
        AQIBreakpoint(0.0, 25.0, 0, 25),
        AQIBreakpoint(25.0, 50.0, 25, 50),
        AQIBreakpoint(50.0, 90.0, 50, 75),
        AQIBreakpoint(90.0, 180.0, 75, 100),
        AQIBreakpoint(180.0, 500.0, 100, 200)
    )

    private val o3Breakpoints = listOf(
        AQIBreakpoint(0.0, 60.0, 0, 25),
        AQIBreakpoint(60.0, 120.0, 25, 50),
        AQIBreakpoint(120.0, 180.0, 50, 75),
        AQIBreakpoint(180.0, 240.0, 75, 100),
        AQIBreakpoint(240.0, 500.0, 100, 200)
    )

    private val no2Breakpoints = listOf(
        AQIBreakpoint(0.0, 50.0, 0, 25),
        AQIBreakpoint(50.0, 100.0, 25, 50),
        AQIBreakpoint(100.0, 200.0, 50, 75),
        AQIBreakpoint(200.0, 400.0, 75, 100),
        AQIBreakpoint(400.0, 800.0, 100, 200)
    )

    private fun calcSubIndex(concentration: Double, breakpoints: List<AQIBreakpoint>): Int {
        for (bp in breakpoints) {
            if (concentration >= bp.cLow && concentration <= bp.cHigh) {
                return (((bp.iHigh - bp.iLow).toDouble() / (bp.cHigh - bp.cLow)) * (concentration - bp.cLow) + bp.iLow).toInt() // AQI equation
            }
        }
        return -1
    }

    fun getAQI(): Int {
        val pm25AQI = calcSubIndex(pm2_5, pm25Breakpoints)
        val pm10AQI = calcSubIndex(pm10, pm10Breakpoints)
        val o3AQI = calcSubIndex(o3, o3Breakpoints)
        //val coAQI = calcSubIndex(co, coBreakpoints)
        //val so2AQI = calcSubIndex(so2, so2Breakpoints)
        val no2AQI = calcSubIndex(no2, no2Breakpoints)

        return maxOf(pm25AQI, pm10AQI, o3AQI, no2AQI)
    }
}