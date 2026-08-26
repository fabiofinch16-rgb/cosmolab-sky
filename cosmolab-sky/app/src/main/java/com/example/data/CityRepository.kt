package com.example.data

import android.content.Context
import android.location.Geocoder
import com.example.astronomy.BortleScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.Normalizer

data class City(
    val name: String,
    val stateOrCountry: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val bortleClass: BortleScale? = null
) {
    val displayName: String
        get() = if (stateOrCountry.isBlank()) name else "$name, $stateOrCountry"
}

object CityRepository {
    val DEFAULT_CITY = City(
        name = "Pomerode",
        stateOrCountry = "SC, Brasil",
        latitude = -26.7389,
        longitude = -49.1764,
        timezoneId = "America/Sao_Paulo",
        bortleClass = BortleScale.BORTLE_4
    )

    val DELFIM_MOREIRA = City(
        name = "Delfim Moreira",
        stateOrCountry = "MG, Brasil",
        latitude = -22.5083,
        longitude = -45.2819,
        timezoneId = "America/Sao_Paulo",
        bortleClass = BortleScale.BORTLE_3
    )

    val ATINS = City(
        name = "Atins",
        stateOrCountry = "MA, Brasil",
        latitude = -2.5719,
        longitude = -42.7442,
        timezoneId = "America/Fortaleza",
        bortleClass = BortleScale.BORTLE_2
    )

    val BARREIRINHAS = City(
        name = "Barreirinhas",
        stateOrCountry = "MA, Brasil",
        latitude = -2.7469,
        longitude = -42.8256,
        timezoneId = "America/Fortaleza",
        bortleClass = BortleScale.BORTLE_3
    )

    val SAO_THOME = City(
        name = "São Thomé das Letras",
        stateOrCountry = "MG, Brasil",
        latitude = -21.7222,
        longitude = -44.9853,
        timezoneId = "America/Sao_Paulo",
        bortleClass = BortleScale.BORTLE_3
    )

    val SAO_PAULO = City(
        name = "São Paulo",
        stateOrCountry = "SP, Brasil",
        latitude = -23.5505,
        longitude = -46.6333,
        timezoneId = "America/Sao_Paulo",
        bortleClass = BortleScale.BORTLE_9
    )

    val SUGGESTED_CITIES = listOf(
        DEFAULT_CITY,
        DELFIM_MOREIRA,
        ATINS,
        BARREIRINHAS,
        SAO_THOME,
        SAO_PAULO,
        City("Rio de Janeiro", "RJ, Brasil", -22.9068, -43.1729, "America/Sao_Paulo", BortleScale.BORTLE_8),
        City("Brasília", "DF, Brasil", -15.7975, -47.8919, "America/Sao_Paulo", BortleScale.BORTLE_7),
        City("Curitiba", "PR, Brasil", -25.4284, -49.2733, "America/Sao_Paulo", BortleScale.BORTLE_7),
        City("Florianópolis", "SC, Brasil", -27.5954, -48.5480, "America/Sao_Paulo", BortleScale.BORTLE_6),
        City("Porto Alegre", "RS, Brasil", -30.0346, -51.2177, "America/Sao_Paulo", BortleScale.BORTLE_7),
        City("Belo Horizonte", "MG, Brasil", -19.9167, -43.9345, "America/Sao_Paulo", BortleScale.BORTLE_8),
        City("Salvador", "BA, Brasil", -12.9777, -38.5016, "America/Bahia", BortleScale.BORTLE_8),
        City("Recife", "PE, Brasil", -8.0476, -34.8770, "America/Recife", BortleScale.BORTLE_8),
        City("Fortaleza", "CE, Brasil", -3.7319, -38.5267, "America/Fortaleza", BortleScale.BORTLE_8),
        City("Manaus", "AM, Brasil", -3.1190, -60.0217, "America/Manaus", BortleScale.BORTLE_7),
        City("Belém", "PA, Brasil", -1.4558, -48.4902, "America/Belem", BortleScale.BORTLE_7),
        City("Goiânia", "GO, Brasil", -16.6869, -49.2648, "America/Sao_Paulo", BortleScale.BORTLE_7),
        City("Campinas", "SP, Brasil", -22.9099, -47.0626, "America/Sao_Paulo", BortleScale.BORTLE_7),
        City("Vitória", "ES, Brasil", -20.3155, -40.3128, "America/Sao_Paulo", BortleScale.BORTLE_6),
        City("Campo Grande", "MS, Brasil", -20.4697, -54.6201, "America/Campo_Grande", BortleScale.BORTLE_6),
        City("Cuiabá", "MT, Brasil", -15.6010, -56.0979, "America/Cuiaba", BortleScale.BORTLE_6),
        City("Natal", "RN, Brasil", -5.7945, -35.2110, "America/Fortaleza", BortleScale.BORTLE_7),
        City("João Pessoa", "PB, Brasil", -7.1195, -34.8450, "America/Fortaleza", BortleScale.BORTLE_7),
        City("Maceió", "AL, Brasil", -9.6658, -35.7353, "America/Maceio", BortleScale.BORTLE_7),
        City("Aracaju", "SE, Brasil", -10.9472, -37.0731, "America/Maceio", BortleScale.BORTLE_7),
        City("Teresina", "PI, Brasil", -5.0892, -42.8019, "America/Fortaleza", BortleScale.BORTLE_7),
        City("São Luís", "MA, Brasil", -2.5307, -44.3068, "America/Fortaleza", BortleScale.BORTLE_7),
        City("Campos do Jordão", "SP, Brasil", -22.7394, -45.5913, "America/Sao_Paulo", BortleScale.BORTLE_4),
        City("Cunha", "SP, Brasil", -23.0739, -44.9578, "America/Sao_Paulo", BortleScale.BORTLE_3),
        City("Monte Verde", "MG, Brasil", -22.8647, -46.0375, "America/Sao_Paulo", BortleScale.BORTLE_3),
        City("Alto Paraíso de Goiás", "GO, Brasil", -14.1331, -47.5147, "America/Sao_Paulo", BortleScale.BORTLE_2),
        City("Lençóis", "BA, Brasil", -12.5606, -41.3894, "America/Bahia", BortleScale.BORTLE_2),
        City("Mateiros", "TO, Brasil", -10.5478, -46.4172, "America/Araguaina", BortleScale.BORTLE_1),
        City("Fernando de Noronha", "PE, Brasil", -3.8576, -32.4297, "America/Noronha", BortleScale.BORTLE_2),
        City("San Pedro de Atacama", "Chile", -22.9087, -68.1997, "America/Santiago", BortleScale.BORTLE_1),
        City("La Palma", "Espanha", 28.6835, -17.7642, "Atlantic/Canary", BortleScale.BORTLE_1),
        City("Mauna Kea", "Havaí, EUA", 19.8207, -155.4681, "Pacific/Honolulu", BortleScale.BORTLE_1),
        City("Lisboa", "Portugal", 38.7223, -9.1393, "Europe/Lisbon", BortleScale.BORTLE_7),
        City("Porto", "Portugal", 41.1579, -8.6291, "Europe/Lisbon", BortleScale.BORTLE_7),
        City("Nova York", "EUA", 40.7128, -74.0060, "America/New_York", BortleScale.BORTLE_9),
        City("Los Angeles", "EUA", 34.0522, -118.2437, "America/Los_Angeles", BortleScale.BORTLE_8),
        City("Paris", "França", 48.8566, 2.3522, "Europe/Paris", BortleScale.BORTLE_8),
        City("Londres", "Reino Unido", 51.5074, -0.1278, "Europe/London", BortleScale.BORTLE_8),
        City("Tóquio", "Japão", 35.6762, 139.6503, "Asia/Tokyo", BortleScale.BORTLE_9),
        City("Sydney", "Austrália", -33.8688, 151.2093, "Australia/Sydney", BortleScale.BORTLE_8),
        City("Santiago", "Chile", -33.4489, -70.6693, "America/Santiago", BortleScale.BORTLE_7),
        City("Buenos Aires", "Argentina", -34.6037, -58.3816, "America/Argentina/Buenos_Aires", BortleScale.BORTLE_8)
    )

    val CITIES = SUGGESTED_CITIES

    private fun removeAccents(str: String): String {
        val normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    fun searchCities(query: String): List<City> {
        if (query.isBlank()) return SUGGESTED_CITIES
        val q = removeAccents(query.lowercase().trim())
        return SUGGESTED_CITIES.filter {
            removeAccents(it.name.lowercase()).contains(q) ||
            removeAccents(it.stateOrCountry.lowercase()).contains(q)
        }
    }

    suspend fun searchCitiesAsync(query: String, context: Context? = null): List<City> {
        if (query.isBlank()) return SUGGESTED_CITIES

        val localMatches = searchCities(query)
        if (query.trim().length < 2) return localMatches

        val onlineResults = mutableListOf<City>()

        // 1. Query Open-Meteo Geocoding REST API (Free, open, no API key required)
        withContext(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(query.trim(), "UTF-8")
                val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=15&language=pt&format=json")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.requestMethod = "GET"

                if (conn.responseCode == 200) {
                    val stream = conn.inputStream
                    val jsonText = stream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonText)
                    if (json.has("results")) {
                        val array = json.getJSONArray("results")
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            val name = item.optString("name", "")
                            val lat = item.optDouble("latitude", 0.0)
                            val lon = item.optDouble("longitude", 0.0)
                            val country = item.optString("country", "")
                            val admin1 = item.optString("admin1", "")
                            val tz = item.optString("timezone", "America/Sao_Paulo")

                            val stateOrCountry = if (admin1.isNotBlank()) {
                                if (country.isNotBlank()) "$admin1, $country" else admin1
                            } else {
                                country
                            }

                            // Check if matches a city in our local database with known Bortle
                            val matchedLocal = SUGGESTED_CITIES.firstOrNull { local ->
                                removeAccents(local.name).equals(removeAccents(name), ignoreCase = true) &&
                                Math.abs(local.latitude - lat) < 0.2 &&
                                Math.abs(local.longitude - lon) < 0.2
                            }

                            val city = matchedLocal ?: City(
                                name = name,
                                stateOrCountry = stateOrCountry,
                                latitude = lat,
                                longitude = lon,
                                timezoneId = if (tz.isNotBlank()) tz else "America/Sao_Paulo",
                                bortleClass = null
                            )
                            onlineResults.add(city)
                        }
                    }
                }
            } catch (e: Exception) {
                // Offline or network error
            }
        }

        // 2. Android Geocoder Fallback if online API didn't return results
        if (onlineResults.isEmpty() && context != null) {
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context)
                    val addresses = geocoder.getFromLocationName(query, 10)
                    if (!addresses.isNullOrEmpty()) {
                        for (addr in addresses) {
                            val cityName = addr.locality ?: addr.subAdminArea ?: addr.featureName ?: query
                            val state = addr.adminArea ?: ""
                            val country = addr.countryName ?: ""
                            val stateOrCountry = listOf(state, country).filter { it.isNotBlank() }.joinToString(", ")
                            val lat = addr.latitude
                            val lon = addr.longitude

                            val matchedLocal = SUGGESTED_CITIES.firstOrNull { local ->
                                removeAccents(local.name).equals(removeAccents(cityName), ignoreCase = true) &&
                                Math.abs(local.latitude - lat) < 0.2 &&
                                Math.abs(local.longitude - lon) < 0.2
                            }

                            val city = matchedLocal ?: City(
                                name = cityName,
                                stateOrCountry = stateOrCountry,
                                latitude = lat,
                                longitude = lon,
                                timezoneId = inferTimeZoneFromCoords(lat, lon),
                                bortleClass = null
                            )
                            onlineResults.add(city)
                        }
                    }
                } catch (e: Exception) {
                    // Geocoder fallback error
                }
            }
        }

        // Merge local matches + online results without duplicates
        val combined = mutableListOf<City>()
        combined.addAll(localMatches)

        for (onlineCity in onlineResults) {
            val exists = combined.any {
                removeAccents(it.displayName).equals(removeAccents(onlineCity.displayName), ignoreCase = true) ||
                (removeAccents(it.name).equals(removeAccents(onlineCity.name), ignoreCase = true) &&
                 Math.abs(it.latitude - onlineCity.latitude) < 0.1 &&
                 Math.abs(it.longitude - onlineCity.longitude) < 0.1)
            }
            if (!exists) {
                combined.add(onlineCity)
            }
        }

        return combined
    }

    private fun inferTimeZoneFromCoords(lat: Double, lon: Double): String {
        if (lon in -74.0..-34.0 && lat in -34.0..5.0) {
            if (lon > -45.0) return "America/Sao_Paulo"
            if (lon in -60.0..-45.0) return "America/Manaus"
            return "America/Rio_Branco"
        }
        if (lon in -10.0..0.0 && lat in 35.0..45.0) return "Europe/Lisbon"
        if (lon in -170.0..-50.0 && lat in 20.0..70.0) return "America/New_York"
        if (lon in 120.0..150.0 && lat in 20.0..50.0) return "Asia/Tokyo"
        if (lon in 110.0..160.0 && lat in -45.0..-10.0) return "Australia/Sydney"
        return "America/Sao_Paulo"
    }
}

