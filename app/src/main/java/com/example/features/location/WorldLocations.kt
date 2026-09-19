package com.example.features.location

import com.example.features.prayer.models.UserLocation

/**
 * One selectable place: capital city + coordinates + IANA timezone.
 * The UTC offset is resolved dynamically ([toUserLocation]) so daylight
 * saving is always correct — no hardcoded GMT offsets.
 */
data class WorldLocation(
    val country: String,
    val capital: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
) {
    fun toUserLocation(): UserLocation {
        val offsetHours = try {
            java.util.TimeZone.getTimeZone(timeZoneId)
                .getOffset(System.currentTimeMillis()) / 3_600_000.0
        } catch (_: Exception) {
            0.0
        }
        return UserLocation(
            latitude = latitude,
            longitude = longitude,
            cityName = capital,
            countryName = country,
            timezoneOffsetHours = offsetHours,
            isAutoDetected = false
        )
    }

    /** e.g. UTC+3, UTC-5, UTC+5:45 — resolved live from [timeZoneId]. */
    fun utcLabel(): String {
        val totalMinutes = try {
            java.util.TimeZone.getTimeZone(timeZoneId)
                .getOffset(System.currentTimeMillis()) / 60_000
        } catch (_: Exception) {
            0
        }
        val sign = if (totalMinutes < 0) "-" else "+"
        val abs = kotlin.math.abs(totalMinutes)
        return if (abs % 60 == 0) "UTC$sign${abs / 60}"
        else "UTC$sign${abs / 60}:${(abs % 60).toString().padStart(2, '0')}"
    }

    fun matches(query: String): Boolean {
        if (query.isBlank()) return true
        return country.contains(query, ignoreCase = true) ||
            capital.contains(query, ignoreCase = true)
    }
}

/** Pinned holy cities shown above the full country list. */
val HOLY_CITIES: List<WorldLocation> = listOf(
    WorldLocation("Saudi Arabia", "Makkah al-Mukarramah", 21.42, 39.83, "Asia/Riyadh"),
    WorldLocation("Saudi Arabia", "Madinah al-Munawwarah", 24.47, 39.61, "Asia/Riyadh"),
    WorldLocation("Palestine", "Al-Quds (Jerusalem)", 31.78, 35.24, "Asia/Hebron")
)

/** All 195 countries (193 UN members + Holy See + Palestine), alphabetical. */
val WORLD_LOCATIONS: List<WorldLocation> = listOf(
    WorldLocation("Afghanistan", "Kabul", 34.53, 69.17, "Asia/Kabul"),
    WorldLocation("Albania", "Tirana", 41.33, 19.82, "Europe/Tirane"),
    WorldLocation("Algeria", "Algiers", 36.75, 3.06, "Africa/Algiers"),
    WorldLocation("Andorra", "Andorra la Vella", 42.51, 1.52, "Europe/Andorra"),
    WorldLocation("Angola", "Luanda", -8.84, 13.23, "Africa/Luanda"),
    WorldLocation("Antigua and Barbuda", "St. John's", 17.12, -61.85, "America/Antigua"),
    WorldLocation("Argentina", "Buenos Aires", -34.60, -58.38, "America/Argentina/Buenos_Aires"),
    WorldLocation("Armenia", "Yerevan", 40.18, 44.51, "Asia/Yerevan"),
    WorldLocation("Australia", "Canberra", -35.28, 149.13, "Australia/Sydney"),
    WorldLocation("Austria", "Vienna", 48.21, 16.37, "Europe/Vienna"),
    WorldLocation("Azerbaijan", "Baku", 40.41, 49.87, "Asia/Baku"),
    WorldLocation("Bahamas", "Nassau", 25.08, -77.35, "America/Nassau"),
    WorldLocation("Bahrain", "Manama", 26.23, 50.59, "Asia/Bahrain"),
    WorldLocation("Bangladesh", "Dhaka", 23.81, 90.41, "Asia/Dhaka"),
    WorldLocation("Barbados", "Bridgetown", 13.10, -59.62, "America/Barbados"),
    WorldLocation("Belarus", "Minsk", 53.90, 27.56, "Europe/Minsk"),
    WorldLocation("Belgium", "Brussels", 50.85, 4.35, "Europe/Brussels"),
    WorldLocation("Belize", "Belmopan", 17.25, -88.77, "America/Belize"),
    WorldLocation("Benin", "Porto-Novo", 6.50, 2.63, "Africa/Porto-Novo"),
    WorldLocation("Bhutan", "Thimphu", 27.47, 89.64, "Asia/Thimphu"),
    WorldLocation("Bolivia", "Sucre", -19.02, -65.26, "America/La_Paz"),
    WorldLocation("Bosnia and Herzegovina", "Sarajevo", 43.86, 18.41, "Europe/Sarajevo"),
    WorldLocation("Botswana", "Gaborone", -24.63, 25.91, "Africa/Gaborone"),
    WorldLocation("Brazil", "Brasília", -15.79, -47.88, "America/Sao_Paulo"),
    WorldLocation("Brunei", "Bandar Seri Begawan", 4.90, 114.94, "Asia/Brunei"),
    WorldLocation("Bulgaria", "Sofia", 42.70, 23.32, "Europe/Sofia"),
    WorldLocation("Burkina Faso", "Ouagadougou", 12.37, -1.53, "Africa/Ouagadougou"),
    WorldLocation("Burundi", "Gitega", -3.43, 29.93, "Africa/Bujumbura"),
    WorldLocation("Cabo Verde", "Praia", 14.92, -23.51, "Atlantic/Cape_Verde"),
    WorldLocation("Cambodia", "Phnom Penh", 11.56, 104.93, "Asia/Phnom_Penh"),
    WorldLocation("Cameroon", "Yaoundé", 3.85, 11.50, "Africa/Douala"),
    WorldLocation("Canada", "Ottawa", 45.42, -75.70, "America/Toronto"),
    WorldLocation("Central African Republic", "Bangui", 4.39, 18.55, "Africa/Bangui"),
    WorldLocation("Chad", "N'Djamena", 12.13, 15.05, "Africa/Ndjamena"),
    WorldLocation("Chile", "Santiago", -33.45, -70.67, "America/Santiago"),
    WorldLocation("China", "Beijing", 39.90, 116.40, "Asia/Shanghai"),
    WorldLocation("Colombia", "Bogotá", 4.71, -74.07, "America/Bogota"),
    WorldLocation("Comoros", "Moroni", -11.70, 43.26, "Indian/Comoro"),
    WorldLocation("Congo (Republic)", "Brazzaville", -4.27, 15.24, "Africa/Brazzaville"),
    WorldLocation("Costa Rica", "San José", 9.93, -84.09, "America/Costa_Rica"),
    WorldLocation("Côte d'Ivoire", "Yamoussoukro", 6.82, -5.28, "Africa/Abidjan"),
    WorldLocation("Croatia", "Zagreb", 45.82, 15.98, "Europe/Zagreb"),
    WorldLocation("Cuba", "Havana", 23.14, -82.37, "America/Havana"),
    WorldLocation("Cyprus", "Nicosia", 35.19, 33.38, "Asia/Nicosia"),
    WorldLocation("Czechia", "Prague", 50.08, 14.44, "Europe/Prague"),
    WorldLocation("DR Congo", "Kinshasa", -4.32, 15.31, "Africa/Kinshasa"),
    WorldLocation("Denmark", "Copenhagen", 55.68, 12.57, "Europe/Copenhagen"),
    WorldLocation("Djibouti", "Djibouti City", 11.59, 43.15, "Africa/Djibouti"),
    WorldLocation("Dominica", "Roseau", 15.30, -61.39, "America/Dominica"),
    WorldLocation("Dominican Republic", "Santo Domingo", 18.49, -69.93, "America/Santo_Domingo"),
    WorldLocation("Ecuador", "Quito", -0.18, -78.47, "America/Guayaquil"),
    WorldLocation("Egypt", "Cairo", 30.04, 31.24, "Africa/Cairo"),
    WorldLocation("El Salvador", "San Salvador", 13.69, -89.22, "America/El_Salvador"),
    WorldLocation("Equatorial Guinea", "Malabo", 3.75, 8.78, "Africa/Malabo"),
    WorldLocation("Eritrea", "Asmara", 15.33, 38.93, "Africa/Asmara"),
    WorldLocation("Estonia", "Tallinn", 59.44, 24.75, "Europe/Tallinn"),
    WorldLocation("Eswatini", "Mbabane", -26.32, 31.13, "Africa/Mbabane"),
    WorldLocation("Ethiopia", "Addis Ababa", 9.01, 38.75, "Africa/Addis_Ababa"),
    WorldLocation("Fiji", "Suva", -18.14, 178.44, "Pacific/Fiji"),
    WorldLocation("Finland", "Helsinki", 60.17, 24.94, "Europe/Helsinki"),
    WorldLocation("France", "Paris", 48.86, 2.35, "Europe/Paris"),
    WorldLocation("Gabon", "Libreville", 0.39, 9.45, "Africa/Libreville"),
    WorldLocation("Gambia", "Banjul", 13.45, -16.58, "Africa/Banjul"),
    WorldLocation("Georgia", "Tbilisi", 41.72, 44.79, "Asia/Tbilisi"),
    WorldLocation("Germany", "Berlin", 52.52, 13.40, "Europe/Berlin"),
    WorldLocation("Ghana", "Accra", 5.60, -0.19, "Africa/Accra"),
    WorldLocation("Greece", "Athens", 37.98, 23.73, "Europe/Athens"),
    WorldLocation("Grenada", "St. George's", 12.06, -61.75, "America/Grenada"),
    WorldLocation("Guatemala", "Guatemala City", 14.63, -90.55, "America/Guatemala"),
    WorldLocation("Guinea", "Conakry", 9.51, -13.72, "Africa/Conakry"),
    WorldLocation("Guinea-Bissau", "Bissau", 11.86, -15.60, "Africa/Bissau"),
    WorldLocation("Guyana", "Georgetown", 6.80, -58.16, "America/Guyana"),
    WorldLocation("Haiti", "Port-au-Prince", 18.54, -72.34, "America/Port-au-Prince"),
    WorldLocation("Holy See", "Vatican City", 41.90, 12.45, "Europe/Vatican"),
    WorldLocation("Honduras", "Tegucigalpa", 14.07, -87.19, "America/Tegucigalpa"),
    WorldLocation("Hungary", "Budapest", 47.50, 19.04, "Europe/Budapest"),
    WorldLocation("Iceland", "Reykjavik", 64.15, -21.94, "Atlantic/Reykjavik"),
    WorldLocation("India", "New Delhi", 28.61, 77.21, "Asia/Kolkata"),
    WorldLocation("Indonesia", "Jakarta", -6.21, 106.85, "Asia/Jakarta"),
    WorldLocation("Iran", "Tehran", 35.69, 51.39, "Asia/Tehran"),
    WorldLocation("Iraq", "Baghdad", 33.31, 44.36, "Asia/Baghdad"),
    WorldLocation("Ireland", "Dublin", 53.35, -6.26, "Europe/Dublin"),
    WorldLocation("Israel", "Jerusalem", 31.77, 35.21, "Asia/Jerusalem"),
    WorldLocation("Italy", "Rome", 41.90, 12.50, "Europe/Rome"),
    WorldLocation("Jamaica", "Kingston", 17.97, -76.79, "America/Jamaica"),
    WorldLocation("Japan", "Tokyo", 35.68, 139.69, "Asia/Tokyo"),
    WorldLocation("Jordan", "Amman", 31.95, 35.93, "Asia/Amman"),
    WorldLocation("Kazakhstan", "Astana", 51.16, 71.43, "Asia/Qyzylorda"),
    WorldLocation("Kenya", "Nairobi", -1.29, 36.82, "Africa/Nairobi"),
    WorldLocation("Kiribati", "Tarawa", 1.45, 173.03, "Pacific/Tarawa"),
    WorldLocation("Kuwait", "Kuwait City", 29.38, 47.99, "Asia/Kuwait"),
    WorldLocation("Kyrgyzstan", "Bishkek", 42.88, 74.61, "Asia/Bishkek"),
    WorldLocation("Laos", "Vientiane", 17.98, 102.61, "Asia/Vientiane"),
    WorldLocation("Latvia", "Riga", 56.95, 24.11, "Europe/Riga"),
    WorldLocation("Lebanon", "Beirut", 33.89, 35.50, "Asia/Beirut"),
    WorldLocation("Lesotho", "Maseru", -29.31, 27.48, "Africa/Maseru"),
    WorldLocation("Liberia", "Monrovia", 6.30, -10.80, "Africa/Monrovia"),
    WorldLocation("Libya", "Tripoli", 32.90, 13.19, "Africa/Tripoli"),
    WorldLocation("Liechtenstein", "Vaduz", 47.14, 9.52, "Europe/Vaduz"),
    WorldLocation("Lithuania", "Vilnius", 54.69, 25.28, "Europe/Vilnius"),
    WorldLocation("Luxembourg", "Luxembourg", 49.61, 6.13, "Europe/Luxembourg"),
    WorldLocation("Madagascar", "Antananarivo", -18.88, 47.51, "Indian/Antananarivo"),
    WorldLocation("Malawi", "Lilongwe", -13.96, 33.77, "Africa/Blantyre"),
    WorldLocation("Malaysia", "Kuala Lumpur", 3.14, 101.69, "Asia/Kuala_Lumpur"),
    WorldLocation("Maldives", "Malé", 4.18, 73.51, "Indian/Maldives"),
    WorldLocation("Mali", "Bamako", 12.64, -8.00, "Africa/Bamako"),
    WorldLocation("Malta", "Valletta", 35.90, 14.51, "Europe/Malta"),
    WorldLocation("Marshall Islands", "Majuro", 7.11, 171.38, "Pacific/Majuro"),
    WorldLocation("Mauritania", "Nouakchott", 18.09, -15.98, "Africa/Nouakchott"),
    WorldLocation("Mauritius", "Port Louis", -20.16, 57.50, "Indian/Mauritius"),
    WorldLocation("Mexico", "Mexico City", 19.43, -99.13, "America/Mexico_City"),
    WorldLocation("Micronesia", "Palikir", 6.92, 158.19, "Pacific/Pohnpei"),
    WorldLocation("Moldova", "Chisinau", 47.01, 28.86, "Europe/Chisinau"),
    WorldLocation("Monaco", "Monaco", 43.74, 7.42, "Europe/Monaco"),
    WorldLocation("Mongolia", "Ulaanbaatar", 47.92, 106.92, "Asia/Ulaanbaatar"),
    WorldLocation("Montenegro", "Podgorica", 42.44, 19.26, "Europe/Podgorica"),
    WorldLocation("Morocco", "Rabat", 34.02, -6.84, "Africa/Casablanca"),
    WorldLocation("Mozambique", "Maputo", -25.97, 32.58, "Africa/Maputo"),
    WorldLocation("Myanmar", "Naypyidaw", 19.76, 96.21, "Asia/Yangon"),
    WorldLocation("Namibia", "Windhoek", -22.56, 17.08, "Africa/Windhoek"),
    WorldLocation("Nauru", "Yaren", -0.55, 166.92, "Pacific/Nauru"),
    WorldLocation("Nepal", "Kathmandu", 27.72, 85.32, "Asia/Kathmandu"),
    WorldLocation("Netherlands", "Amsterdam", 52.37, 4.90, "Europe/Amsterdam"),
    WorldLocation("New Zealand", "Wellington", -41.29, 174.78, "Pacific/Auckland"),
    WorldLocation("Nicaragua", "Managua", 12.14, -86.28, "America/Managua"),
    WorldLocation("Niger", "Niamey", 13.51, 2.11, "Africa/Niamey"),
    WorldLocation("Nigeria", "Abuja", 9.06, 7.49, "Africa/Lagos"),
    WorldLocation("North Korea", "Pyongyang", 39.03, 125.75, "Asia/Pyongyang"),
    WorldLocation("North Macedonia", "Skopje", 42.00, 21.43, "Europe/Skopje"),
    WorldLocation("Norway", "Oslo", 59.91, 10.75, "Europe/Oslo"),
    WorldLocation("Oman", "Muscat", 23.59, 58.28, "Asia/Muscat"),
    WorldLocation("Pakistan", "Islamabad", 33.68, 73.05, "Asia/Karachi"),
    WorldLocation("Palau", "Ngerulmud", 7.50, 134.62, "Pacific/Palau"),
    WorldLocation("Palestine", "Ramallah", 31.90, 35.20, "Asia/Hebron"),
    WorldLocation("Panama", "Panama City", 8.98, -79.52, "America/Panama"),
    WorldLocation("Papua New Guinea", "Port Moresby", -9.48, 147.15, "Pacific/Port_Moresby"),
    WorldLocation("Paraguay", "Asunción", -25.26, -57.58, "America/Asuncion"),
    WorldLocation("Peru", "Lima", -12.05, -77.04, "America/Lima"),
    WorldLocation("Philippines", "Manila", 14.60, 120.98, "Asia/Manila"),
    WorldLocation("Poland", "Warsaw", 52.23, 21.01, "Europe/Warsaw"),
    WorldLocation("Portugal", "Lisbon", 38.72, -9.14, "Europe/Lisbon"),
    WorldLocation("Qatar", "Doha", 25.29, 51.53, "Asia/Qatar"),
    WorldLocation("Romania", "Bucharest", 44.43, 26.10, "Europe/Bucharest"),
    WorldLocation("Russia", "Moscow", 55.76, 37.62, "Europe/Moscow"),
    WorldLocation("Rwanda", "Kigali", -1.95, 30.06, "Africa/Kigali"),
    WorldLocation("Saint Kitts and Nevis", "Basseterre", 17.30, -62.72, "America/St_Kitts"),
    WorldLocation("Saint Lucia", "Castries", 14.01, -60.99, "America/St_Lucia"),
    WorldLocation("Saint Vincent and the Grenadines", "Kingstown", 13.16, -61.23, "America/St_Vincent"),
    WorldLocation("Samoa", "Apia", -13.83, -171.77, "Pacific/Apia"),
    WorldLocation("San Marino", "San Marino", 43.94, 12.45, "Europe/San_Marino"),
    WorldLocation("Sao Tome and Principe", "São Tomé", 0.34, 6.73, "Africa/Sao_Tome"),
    WorldLocation("Saudi Arabia", "Riyadh", 24.71, 46.68, "Asia/Riyadh"),
    WorldLocation("Senegal", "Dakar", 14.69, -17.45, "Africa/Dakar"),
    WorldLocation("Serbia", "Belgrade", 44.79, 20.45, "Europe/Belgrade"),
    WorldLocation("Seychelles", "Victoria", -4.62, 55.45, "Indian/Mahe"),
    WorldLocation("Sierra Leone", "Freetown", 8.48, -13.23, "Africa/Freetown"),
    WorldLocation("Singapore", "Singapore", 1.35, 103.82, "Asia/Singapore"),
    WorldLocation("Slovakia", "Bratislava", 48.15, 17.11, "Europe/Bratislava"),
    WorldLocation("Slovenia", "Ljubljana", 46.06, 14.51, "Europe/Ljubljana"),
    WorldLocation("Solomon Islands", "Honiara", -9.47, 159.95, "Pacific/Guadalcanal"),
    WorldLocation("Somalia", "Mogadishu", 2.04, 45.34, "Africa/Mogadishu"),
    WorldLocation("South Africa", "Pretoria", -25.75, 28.19, "Africa/Johannesburg"),
    WorldLocation("South Korea", "Seoul", 37.57, 126.98, "Asia/Seoul"),
    WorldLocation("South Sudan", "Juba", 4.85, 31.58, "Africa/Juba"),
    WorldLocation("Spain", "Madrid", 40.42, -3.70, "Europe/Madrid"),
    WorldLocation("Sri Lanka", "Sri Jayawardenepura Kotte", 6.91, 79.86, "Asia/Colombo"),
    WorldLocation("Sudan", "Khartoum", 15.50, 32.56, "Africa/Khartoum"),
    WorldLocation("Suriname", "Paramaribo", 5.85, -55.20, "America/Paramaribo"),
    WorldLocation("Sweden", "Stockholm", 59.33, 18.07, "Europe/Stockholm"),
    WorldLocation("Switzerland", "Bern", 46.95, 7.45, "Europe/Zurich"),
    WorldLocation("Syria", "Damascus", 33.51, 36.29, "Asia/Damascus"),
    WorldLocation("Tajikistan", "Dushanbe", 38.56, 68.78, "Asia/Dushanbe"),
    WorldLocation("Tanzania", "Dodoma", -6.17, 35.74, "Africa/Dar_es_Salaam"),
    WorldLocation("Thailand", "Bangkok", 13.76, 100.50, "Asia/Bangkok"),
    WorldLocation("Timor-Leste", "Dili", -8.56, 125.58, "Asia/Dili"),
    WorldLocation("Togo", "Lomé", 6.13, 1.22, "Africa/Lome"),
    WorldLocation("Tonga", "Nuku'alofa", -21.14, -175.20, "Pacific/Tongatapu"),
    WorldLocation("Trinidad and Tobago", "Port of Spain", 10.65, -61.52, "America/Port_of_Spain"),
    WorldLocation("Tunisia", "Tunis", 36.80, 10.18, "Africa/Tunis"),
    WorldLocation("Turkey", "Ankara", 39.93, 32.86, "Europe/Istanbul"),
    WorldLocation("Turkmenistan", "Ashgabat", 37.95, 58.38, "Asia/Ashgabat"),
    WorldLocation("Tuvalu", "Funafuti", -8.52, 179.19, "Pacific/Funafuti"),
    WorldLocation("Uganda", "Kampala", 0.35, 32.58, "Africa/Kampala"),
    WorldLocation("Ukraine", "Kyiv", 50.45, 30.52, "Europe/Kiev"),
    WorldLocation("United Arab Emirates", "Abu Dhabi", 24.45, 54.38, "Asia/Dubai"),
    WorldLocation("United Kingdom", "London", 51.51, -0.13, "Europe/London"),
    WorldLocation("United States", "Washington, D.C.", 38.91, -77.04, "America/New_York"),
    WorldLocation("Uruguay", "Montevideo", -34.90, -56.16, "America/Montevideo"),
    WorldLocation("Uzbekistan", "Tashkent", 41.30, 69.24, "Asia/Tashkent"),
    WorldLocation("Vanuatu", "Port Vila", -17.73, 168.32, "Pacific/Efate"),
    WorldLocation("Venezuela", "Caracas", 10.49, -66.88, "America/Caracas"),
    WorldLocation("Vietnam", "Hanoi", 21.03, 105.85, "Asia/Ho_Chi_Minh"),
    WorldLocation("Yemen", "Sana'a", 15.35, 44.21, "Asia/Aden"),
    WorldLocation("Zambia", "Lusaka", -15.39, 28.32, "Africa/Lusaka"),
    WorldLocation("Zimbabwe", "Harare", -17.83, 31.05, "Africa/Harare"),
)
