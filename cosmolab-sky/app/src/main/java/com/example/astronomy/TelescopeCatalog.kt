package com.example.astronomy

data class TelescopeModel(
    val id: String,
    val fabricante: String,
    val modelo: String,
    val aberturaMm: Int,
    val distanciaFocalMm: Int? = null,
    val razaoFocal: Double? = null,
    val tipoOptico: String,
    val montagem: String,
    val nivelSugerido: String,
    val ampliacaoUtilTeoricaX: Int? = null,
    val indicacoesBase: String? = null,
    val astrofotografia: String? = null,
    val precoBrl: Double? = null,
    val precoData: String? = null,
    val fontePreco: String? = null,
    val observacao: String? = null
) {
    val fullName: String
        get() = "$fabricante $modelo"

    val computedRazaoFocal: Double?
        get() = razaoFocal ?: if (distanciaFocalMm != null && aberturaMm > 0) distanciaFocalMm.toDouble() / aberturaMm else null

    val isGoTo: Boolean
        get() = montagem.contains("GoTo", ignoreCase = true) ||
                montagem.contains("Go-To", ignoreCase = true) ||
                montagem.contains("StarSense", ignoreCase = true) ||
                montagem.contains("motorizad", ignoreCase = true)

    val isDobsonian: Boolean
        get() = tipoOptico.contains("Dobson", ignoreCase = true) ||
                montagem.contains("Dobson", ignoreCase = true) ||
                modelo.contains("Heritage", ignoreCase = true) ||
                modelo.contains("FlexTube", ignoreCase = true)

    val isEquatorial: Boolean
        get() = montagem.contains("Equatorial", ignoreCase = true) ||
                montagem.contains("EQ", ignoreCase = true)

    val isAltAzimuth: Boolean
        get() = montagem.contains("Alt-Azimuth", ignoreCase = true) ||
                montagem.contains("AZ", ignoreCase = true)

    val isPortable: Boolean
        get() = nivelSugerido.contains("portátil", ignoreCase = true) ||
                modelo.contains("Travel", ignoreCase = true) ||
                modelo.contains("Heritage", ignoreCase = true) ||
                tipoOptico.contains("Maksutov", ignoreCase = true) ||
                (aberturaMm <= 90 && !isEquatorial)
}

object TelescopeCatalog {
    val models: List<TelescopeModel> = listOf(
        TelescopeModel("celestron_starsense_explorer_dx_6_sct", "Celestron", "StarSense Explorer DX 6 SCT", 152, 1500, 9.87, "Schmidt-Cassegrain", "GoTo", "avançado", 304, "Lua; planetas", "potencial; avaliar montagem", 16999.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_powerseeker_60az", "Celestron", "PowerSeeker 60AZ", 60, 700, 11.67, "Refrator", "Alt-Azimuth manual", "iniciante", 120, "Lua; planetas brilhantes", "não classificar", 1799.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_travel_scope_80", "Celestron", "Travel Scope 80", 80, 400, 5.0, "Refrator", "Alt-Azimuth manual", "iniciante/portátil", 160, "Lua; planetas brilhantes; objetos brilhantes de céu profundo", "não classificar", 2599.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_travel_scope_70", "Celestron", "Travel Scope 70", 70, 400, 5.71, "Refrator", "Alt-Azimuth manual", "iniciante/portátil", 140, "Lua; planetas brilhantes; objetos brilhantes de céu profundo", "não classificar", 1799.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_astromaster_130eq", "Celestron", "AstroMaster 130EQ", 130, 650, 5.0, "Newtoniano", "Equatorial manual", "intermediário", 260, "Lua; planetas", "potencial; avaliar montagem", 5699.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_starsense_explorer_lt_70az", "Celestron", "StarSense Explorer LT 70AZ", 70, 700, 10.0, "Refrator", "Alt-Azimuth manual", "iniciante", 140, "Lua; planetas brilhantes", "não classificar", 2999.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_starsense_explorer_lt127az", "Celestron", "StarSense Explorer LT127AZ", 127, 1000, 7.87, "Newtoniano", "Alt-Azimuth manual", "intermediário", 254, "Lua; planetas", "não classificar", 4899.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_nexstar_4se_go-to_mak", "Celestron", "NexStar 4SE Go-To Mak", 102, 1325, 12.99, "Maksutov-Cassegrain", "GoTo", "intermediário/avançado", 204, "Lua; planetas", "potencial; avaliar montagem", 11999.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_nexstar_6se_sct_go-to", "Celestron", "NexStar 6SE SCT Go-To", 150, 1500, 10.0, "Schmidt-Cassegrain", "GoTo", "avançado", 300, "Lua; planetas", "potencial; avaliar montagem", 23999.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_advanced_vx_6_eq_go-to", "Celestron", "Advanced VX 6 EQ Go-To", 150, 1500, 10.0, "Schmidt-Cassegrain", "GoTo equatorial", "avançado", 300, "Lua; planetas", "potencial; avaliar montagem", 22999.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_astromaster_90az", "Celestron", "AstroMaster 90AZ", 90, 1000, 11.11, "Refrator", "Alt-Azimuth manual", "iniciante/intermediário", 180, "Lua; planetas brilhantes", "não classificar", 5399.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_astromaster_lt_70az", "Celestron", "AstroMaster LT 70AZ", 70, 700, 10.0, "Refrator", "Alt-Azimuth manual", "iniciante", 140, "Lua; planetas brilhantes", "não classificar", 2499.0, "2026-08-12", "Loja oficial Celestron Brasil"),
        TelescopeModel("celestron_starsense_explorer_dx_5_sct", "Celestron", "StarSense Explorer DX 5 SCT", 127, 1250, 9.84, "Schmidt-Cassegrain", "Alt-Azimuth manual", "intermediário", 254, "Lua; planetas", "não classificar", 13499.0, "2026-08-12", "Loja oficial Celestron Brasil"),

        TelescopeModel("meade_infinity_70_700_az", "Meade", "Infinity 70/700 AZ", 70, 700, 10.0, "Refrator", "Alt-Azimuth", "iniciante", 140, "Lua; planetas brilhantes", "não classificar", 2610.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("meade_n_114_1000_polaris_eq", "Meade", "N 114/1000 Polaris EQ", 114, 1000, 8.77, "Newtoniano", "Equatorial", "iniciante/intermediário", 228, "Lua; planetas", "potencial", 6312.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("meade_ac_90_900_polaris_eq", "Meade", "AC 90/900 Polaris EQ", 90, 900, 10.0, "Refrator", "Equatorial", "iniciante/intermediário", 180, "Lua; planetas brilhantes", "potencial", 6517.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("meade_n_127_1000_polaris_eq", "Meade", "N 127/1000 Polaris EQ", 127, 1000, 7.87, "Newtoniano", "Equatorial", "intermediário", 254, "Lua; planetas", "potencial", 6834.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("meade_acf-sc_152_1524_uhtc_ota", "Meade", "ACF-SC 152/1524 UHTC OTA", 152, 1524, 10.03, "ACF/Schmidt-Cassegrain", "OTA", "avançado", 304, "Lua; planetas", "potencial", 20564.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("meade_goto_acf-sc_203_2034_uhtc_lx90", "Meade", "GoTo ACF-SC 203/2034 UHTC LX90", 203, 2034, 10.02, "ACF/Schmidt-Cassegrain", "GoTo", "avançado", 406, "Lua; planetas", "potencial", 55553.0, "2026-08-12", "imagem/loja consultada"),

        TelescopeModel("sky-watcher_70_700_az", "Sky-Watcher", "70/700 AZ", 70, 700, 10.0, "Refrator", "Alt-Azimuth", "iniciante", 140, "Lua; planetas brilhantes", "não classificar", 2294.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_70_900_eq1", "Sky-Watcher", "70/900 EQ1", 70, 900, 12.86, "Refrator", "Equatorial", "iniciante", 140, "Lua; planetas brilhantes", "potencial", 3085.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_90_900_ota", "Sky-Watcher", "90/900 OTA", 90, 900, 10.0, "Refrator", "OTA", "iniciante/intermediário", 180, "Lua; planetas brilhantes", "potencial", 3465.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_mak90_black_diamond_ota", "Sky-Watcher", "Mak90 Black Diamond OTA", 90, 1250, 13.89, "Maksutov-Cassegrain", "OTA", "intermediário", 180, "Lua; planetas brilhantes", "potencial", 3955.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_80_400_az3", "Sky-Watcher", "80/400 AZ3", 80, 400, 5.0, "Refrator", "Alt-Azimuth", "iniciante/portátil", 160, "Lua; planetas brilhantes", "não classificar", 4066.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_114_900_eq1", "Sky-Watcher", "114/900 EQ1", 114, 900, 7.89, "Newtoniano", "Equatorial", "iniciante/intermediário", 228, "Lua; planetas", "potencial", 4249.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_130_900_eq2", "Sky-Watcher", "130/900 EQ2", 130, 900, 6.92, "Newtoniano", "Equatorial", "intermediário", 260, "Lua; planetas", "potencial", 4524.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_heritage_130_650_flextube", "Sky-Watcher", "Heritage 130/650 FlexTube", 130, 650, 5.0, "Newtoniano Dobsoniano", "Dobsoniano", "intermediário", 260, "Lua; planetas; céu profundo", "potencial", 4572.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_90_900_eq2", "Sky-Watcher", "90/900 EQ2", 90, 900, 10.0, "Refrator", "Equatorial", "intermediário", 180, "Lua; planetas brilhantes", "potencial", 4730.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_newton_130_650_starquest", "Sky-Watcher", "Newton 130/650 StarQuest", 130, 650, 5.0, "Newtoniano", "StarQuest", "intermediário", 260, "Lua; planetas", "potencial", 4983.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_mak90_1250_starquest", "Sky-Watcher", "Mak90/1250 StarQuest", 90, 1250, 13.89, "Maksutov-Cassegrain", "StarQuest", "intermediário", 180, "Lua; planetas brilhantes", "potencial", 5220.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_dobsoniano_150_750_flextube_heritage", "Sky-Watcher", "Dobsoniano 150/750 FlexTube Heritage", 150, 750, 5.0, "Newtoniano Dobsoniano", "Dobsoniano", "intermediário", 300, "Lua; planetas; céu profundo", "potencial", 5711.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_130_900_eq2_motorizado_em_ad", "Sky-Watcher", "130/900 EQ2 motorizado em AD", 130, 900, 6.92, "Newtoniano", "Equatorial motorizada", "intermediário", 260, "Lua; planetas", "potencial", 5774.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_ota_n_150_750_black_diamond", "Sky-Watcher", "OTA N 150/750 Black Diamond", 150, 750, 5.0, "Newtoniano", "OTA", "intermediário", 300, "Lua; planetas", "potencial", 6320.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_dobsoniano_150_mm", "Sky-Watcher", "Dobsoniano 150 mm", 150, 1200, 8.0, "Newtoniano Dobsoniano", "Dobsoniano", "intermediário", 300, "Lua; planetas; céu profundo", "potencial", 7182.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_mak127_1500", "Sky-Watcher", "Mak127/1500", 127, 1500, 11.81, "Maksutov-Cassegrain", "OTA/montagem", "intermediário/avançado", 254, "Lua; planetas", "potencial", 7261.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_mak127_1500_black_diamond", "Sky-Watcher", "Mak127/1500 Black Diamond", 127, 1500, 11.81, "Maksutov-Cassegrain", "OTA", "intermediário/avançado", 254, "Lua; planetas", "potencial", 7261.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_apo_62_400_evolux-62ed_ota", "Sky-Watcher", "APO 62/400 Evolux-62ED OTA", 62, 400, 6.45, "Refrator apocromático", "OTA", "astrofotografia", 124, "Astrofotografia; ampla visão", "potencial", 7261.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_refrator_120_1000_ota", "Sky-Watcher", "Refrator 120/1000 OTA", 120, 1000, 8.33, "Refrator", "OTA", "intermediário", 240, "Lua; planetas", "potencial", 7277.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_ota_newton_200_1000_black_diamond", "Sky-Watcher", "OTA Newton 200/1000 Black Diamond", 200, 1000, 5.0, "Newtoniano", "OTA", "avançado", 400, "Lua; planetas; céu profundo", "potencial", 7577.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_120_600_az3", "Sky-Watcher", "120/600 AZ3", 120, 600, 5.0, "Refrator", "Alt-Azimuth", "intermediário", 240, "Lua; planetas; campos amplos", "não classificar", 7830.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_refrator_102_500_az-go2", "Sky-Watcher", "Refrator 102/500 AZ-GO2", 102, 500, 4.9, "Refrator", "GoTo", "intermediário", 204, "Lua; planetas; automatizado", "potencial", 8210.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_virtuoso_gti_150", "Sky-Watcher", "Virtuoso GTi 150", 150, 750, 5.0, "Newtoniano", "GoTo", "intermediário/avançado", 300, "Lua; planetas; céu profundo; GoTo", "potencial", 8463.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_150_750_eq3-2", "Sky-Watcher", "150/750 EQ3-2", 150, 750, 5.0, "Newtoniano", "Equatorial", "intermediário", 300, "Lua; planetas", "potencial", 9475.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_150_750_eq3-2_motorizado", "Sky-Watcher", "150/750 EQ3-2 motorizado", 150, 750, 5.0, "Newtoniano", "Equatorial motorizada", "intermediário", 300, "Lua; planetas", "potencial", 5774.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_150_750_eqm-35_pro_go-to", "Sky-Watcher", "150/750 EQM-35 Pro Go-To", 150, 750, 5.0, "Newtoniano", "GoTo equatorial", "avançado", 300, "Lua; planetas; astrofotografia", "potencial", 19457.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_200_1000_neq5", "Sky-Watcher", "200/1000 NEQ5", 200, 1000, 5.0, "Newtoniano", "Equatorial", "avançado", 400, "Lua; planetas; céu profundo", "potencial", 12718.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_mak150_black_diamond", "Sky-Watcher", "Mak150 Black Diamond", 150, 1800, 12.0, "Maksutov-Cassegrain", "OTA", "avançado", 300, "Lua; planetas detalhados", "potencial", 13446.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_120_1000_neq5", "Sky-Watcher", "120/1000 NEQ5", 120, 1000, 8.33, "Refrator", "Equatorial", "avançado", 240, "Lua; planetas", "potencial", 13604.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_dobsoniano_200_mm", "Sky-Watcher", "Dobsoniano 200 mm", 200, 1200, 6.0, "Newtoniano Dobsoniano", "Dobsoniano", "avançado", 400, "Lua; planetas; céu profundo excelente", "potencial", 9855.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_150_750_neq3-2", "Sky-Watcher", "150/750 NEQ3-2", 150, 750, 5.0, "Newtoniano", "Equatorial", "intermediário", 300, "Lua; planetas", "potencial", 12481.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_200_1000_neq5_black_diamond", "Sky-Watcher", "200/1000 NEQ5 Black Diamond", 200, 1000, 5.0, "Newtoniano", "Equatorial", "avançado", 400, "Lua; planetas; céu profundo", "potencial", 12718.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_100ed_900_black_diamond", "Sky-Watcher", "100ED 900 Black Diamond", 100, 900, 9.0, "Refrator apocromático", "OTA", "astrofotografia/intermediário", 200, "Lua; planetas; astrofotografia", "potencial", 16594.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_mak127_eq3-2_go-to", "Sky-Watcher", "Mak127 EQ3-2 Go-To", 127, 1500, 11.81, "Maksutov-Cassegrain", "GoTo", "avançado", 254, "Lua; planetas; automatizado", "potencial", 17131.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_200_1000_f_5_neq5_pro_go-to", "Sky-Watcher", "200/1000 f/5 NEQ5 Pro Go-To", 200, 1000, 5.0, "Newtoniano", "GoTo equatorial", "avançado", 400, "Lua; planetas; céu profundo", "potencial", 19377.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_dobsoniano_flextube_200", "Sky-Watcher", "Dobsoniano FlexTube 200", 200, 1200, 6.0, "Newtoniano Dobsoniano", "Dobsoniano", "avançado", 400, "Lua; planetas; céu profundo; retrátil", "potencial", 11342.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_150_750_neq3-2_black_diamond", "Sky-Watcher", "150/750 NEQ3-2 Black Diamond", 150, 750, 5.0, "Newtoniano", "Equatorial", "intermediário", 300, "Lua; planetas", "potencial", 12481.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_150_750_neq5_black_diamond", "Sky-Watcher", "150/750 NEQ5 Black Diamond", 150, 750, 5.0, "Newtoniano", "Equatorial", "avançado", 300, "Lua; planetas", "potencial", 12800.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_ota_150_750", "Sky-Watcher", "OTA 150/750", 150, 750, 5.0, "Newtoniano", "OTA", "intermediário", 300, "Lua; planetas", "potencial", 13604.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_dobsoniano_254_mm", "Sky-Watcher", "Dobsoniano 254 mm", 254, 1250, 4.92, "Newtoniano Dobsoniano", "Dobsoniano", "avançado", 508, "Céu profundo de alta capacidade; planetas", "potencial", 14063.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_150_750_no_eq3-2_pro_go-to_black_diamond", "Sky-Watcher", "150/750 no EQ3-2 Pro Go-To Black Diamond", 150, 750, 5.0, "Newtoniano", "GoTo", "avançado", 300, "Lua; planetas; GoTo", "potencial", 15028.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_200_1000_f_5_dupla_velocidade_neq5_pro_go-to_bd", "Sky-Watcher", "200/1000 f/5 dupla velocidade NEQ5 Pro Go-To BD", 200, 1000, 5.0, "Newtoniano", "GoTo equatorial", "avançado", 400, "Lua; planetas; astrofotografia", "potencial", 21671.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_dobsoniano_flextube_250_mm", "Sky-Watcher", "Dobsoniano FlexTube 250 mm", 250, 1250, 5.0, "Newtoniano Dobsoniano", "Dobsoniano", "avançado", 500, "Céu profundo; planetas; retrátil", "potencial", 15265.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_ota_apocromatico_ap_80ed_black_diamond", "Sky-Watcher", "OTA apocromático AP 80ED Black Diamond", 80, 600, 7.5, "Refrator apocromático", "OTA", "astrofotografia", 160, "Astrofotografia; galáxias amplas", "potencial", 10298.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_200_mm_f_5_dupla_velocidade_em_heq5_pro_go-to_bd", "Sky-Watcher", "200 mm f/5 dupla velocidade em HEQ5 Pro Go-To BD", 200, 1000, 5.0, "Newtoniano", "GoTo equatorial", "avançado", 400, "Lua; planetas; astrofotografia", "potencial", 26875.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_mak150_black_diamond_no_neq3-2_pro_go-to", "Sky-Watcher", "Mak150 Black Diamond no NEQ3-2 Pro Go-To", 150, 1800, 12.0, "Maksutov-Cassegrain", "GoTo", "avançado", 300, "Lua; planetas", "potencial", 27097.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_mak150_black_diamond_no_neq5_pro_go-to", "Sky-Watcher", "Mak150 Black Diamond no NEQ5 Pro Go-To", 150, 1800, 12.0, "Maksutov-Cassegrain", "GoTo", "avançado", 300, "Lua; planetas", "potencial", 28805.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_apo_120_900_black_diamond_ed_ds_ota", "Sky-Watcher", "APO 120/900 Black Diamond ED DS OTA", 120, 900, 7.5, "Refrator apocromático", "OTA", "astrofotografia", 240, "Astrofotografia premium; planetas", "potencial", 30830.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_mak180_black_diamond_no_neq5_pro_go-to", "Sky-Watcher", "Mak180 Black Diamond no NEQ5 Pro Go-To", 180, 2700, 15.0, "Maksutov-Cassegrain", "GoTo", "avançado", 360, "Planetas de alta resolução; Lua", "potencial", 37963.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_200_mm_f_5_dupla_velocidade_azeq6_pro_go-to", "Sky-Watcher", "200 mm f/5 dupla velocidade AZEQ6 Pro Go-To", 200, 1000, 5.0, "Newtoniano", "GoTo", "avançado", 400, "Lua; planetas; astrofotografia", "potencial", 42234.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_250_mm_f_5_dupla_velocidade_azeq6_pro_go-to_bd", "Sky-Watcher", "250 mm f/5 dupla velocidade AZEQ6 Pro Go-To BD", 250, 1250, 5.0, "Newtoniano", "GoTo", "avançado", 500, "Céu profundo; planetas", "potencial", 47438.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_dobsoniano_flextube_350_1650", "Sky-Watcher", "Dobsoniano FlexTube 350/1650", 350, 1650, 4.71, "Newtoniano Dobsoniano", "Dobsoniano", "avançado", 700, "Céu profundo profundo; nebulosas e galáxias", "potencial", 49431.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_dobsoniano_flextube_go-to_400_mm", "Sky-Watcher", "Dobsoniano FlexTube Go-To 400 mm", 400, 1800, 4.5, "Newtoniano Dobsoniano", "GoTo", "avançado", 800, "Céu profundo extremo; automatizado", "potencial", 60567.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_stargate_go-to_400_mm", "Sky-Watcher", "Stargate Go-To 400 mm", 400, 1800, 4.5, "Newtoniano Dobsoniano", "GoTo", "avançado", 800, "Céu profundo extremo; estrutura em treliça", "potencial", 66989.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("sky-watcher_stargate_500_mm", "Sky-Watcher", "Stargate 500 mm", 500, 2000, 4.0, "Newtoniano Dobsoniano", "Dobsoniano/GoTo não confirmado", "avançado", 1000, "Abertura gigante de 500mm para céu profundo", "potencial", 110552.0, "2026-08-12", "imagem/loja consultada"),

        TelescopeModel("greika_tele_70070___d70_f70m", "Greika", "TELE 70070 / D70 F70M", 70, 700, 10.0, "Refrator", "Alt-Azimuth", "iniciante", 140, "Lua; planetas brilhantes", "não classificar", 1001.87, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("greika_bm1400150eqiiia___1400x150_eq", "Greika", "BM1400150EQIIIA / 1400x150 EQ", 150, 1400, 9.33, "Newtoniano", "Equatorial EQ3", "intermediário", 300, "Lua; planetas", "potencial", 2699.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("greika_tele-90060", "Greika", "TELE-90060", 60, 900, 15.0, "Refrator", "Alt-Azimuth", "iniciante", 120, "Lua; planetas brilhantes", "não classificar", 769.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("greika_maksutov_90", "Greika", "Maksutov 90", 90, null, null, "Maksutov-Cassegrain", "Equatorial", "intermediário", 180, "Lua; planetas brilhantes", "potencial", 2899.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("greika_tele1000114___1000x114", "Greika", "TELE1000114 / 1000x114", 114, 1000, 8.77, "Newtoniano", "Equatorial", "iniciante/intermediário", 228, "Lua; planetas", "potencial", 2299.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("greika_bm-36060d", "Greika", "BM-36060D", 60, 360, 6.0, "Refrator", "Alt-Azimuth", "iniciante/portátil", 120, "Lua; planetas brilhantes", "não classificar", 404.81, "2026-08-12", "imagem/loja consultada"),

        TelescopeModel("uranum_atena_70_700", "Uranum", "Atena 70/700", 70, 700, 10.0, "Refrator", "Alt-Azimuth", "iniciante", 140, "Lua; planetas brilhantes", "não classificar", 999.99, "2026-08-12", "Loja oficial Uranum"),
        TelescopeModel("uranum_newtoniano_130", "Uranum", "Newtoniano 130", 130, null, null, "Newtoniano", "não confirmado", "intermediário", 260, "Lua; planetas", "potencial", 2399.99, "2026-08-12", "Loja oficial Uranum"),
        TelescopeModel("uranum_aries_120_900", "Uranum", "Aries 120/900", 120, 900, 7.5, "Refrator", "não confirmado", "intermediário", 240, "Lua; planetas", "potencial", 2799.99, "2026-08-12", "Loja oficial Uranum"),
        TelescopeModel("uranum_zeus_210", "Uranum", "Zeus 210", 210, null, null, "Newtoniano", "não confirmado", "avançado", 420, "Lua; planetas; céu profundo", "potencial", 6399.99, "2026-08-12", "Loja oficial Uranum"),
        TelescopeModel("uranum_galileu_160_800", "Uranum", "Galileu 160/800", 160, 800, 5.0, "Newtoniano", "Equatorial", "intermediário/avançado", 320, "Lua; planetas; céu profundo", "potencial", 4499.99, "2026-08-12", "Loja oficial Uranum"),
        TelescopeModel("uranum_newtoniano_114_900", "Uranum", "Newtoniano 114/900", 114, 900, 7.89, "Newtoniano", "Equatorial", "intermediário", 228, "Lua; planetas", "potencial", 1899.99, "2026-08-12", "Loja oficial Uranum"),

        TelescopeModel("toya_galaxy_ultrapotec_60_hrt60l_-_675x", "Toya", "Galaxy Ultrapotec 60 HRT60L - 675X", 60, 675, 11.25, "Refrator", "Alt-Azimuth", "iniciante", 120, "Lua; planetas brilhantes", "não classificar", 820.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("toya_orbit_oribitor_70_mm_s70thrc", "Toya", "Orbit/Oribitor 70 mm S70THRC", 70, null, null, "Refrator", "não confirmado", "iniciante", 140, "Lua; planetas brilhantes", "potencial", 545.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("toya_orbit_oribitor_70_mm_s70thrc_power_200x", "Toya", "Orbit/Oribitor 70 mm S70THRC Power 200X", 70, null, null, "Refrator", "não confirmado", "iniciante", 140, "Lua; planetas brilhantes", "potencial", 730.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("toya_galaxy_ultrapotec_70_mm_hrt70l", "Toya", "Galaxy Ultrapotec 70 mm HRT70L", 70, null, null, "Refrator", "não confirmado", "iniciante", 140, "Lua; planetas brilhantes", "potencial", 1180.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("toya_skyview_rf_114_hrt_114az2p_advanced", "Toya", "Skyview RF 114 HRT 114AZ2P Advanced", 114, null, null, "Newtoniano", "Alt-Azimuth", "iniciante/intermediário", 228, "Lua; planetas", "não classificar", 1700.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("toya_startec_rf_150_mm_pro_150st_eq3-5_black_diamond", "Toya", "Startec RF 150 mm Pro 150ST EQ3-5 Black Diamond", 150, null, null, "Newtoniano", "Equatorial", "intermediário", 300, "Lua; planetas", "potencial", 3300.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("toya_startec_rf_150_mm_pro_150lt_eq3-5_black_diamond", "Toya", "Startec RF 150 mm Pro 150LT EQ3-5 Black Diamond", 150, null, null, "Newtoniano", "Equatorial", "intermediário", 300, "Lua; planetas", "potencial", 3350.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("toya_startec_rf_150_mm_pro_+_motor_clock", "Toya", "Startec RF 150 mm Pro + Motor Clock", 150, null, null, "Newtoniano", "Equatorial motorizada", "intermediário", 300, "Lua; planetas", "potencial", 4380.0, "2026-08-12", "imagem/loja consultada"),

        TelescopeModel("andromeda_telescopios_newtoniano_150_mm", "Andrômeda Telescópios", "Newtoniano 150 mm", 150, 950, 6.33, "Newtoniano", "manual", "intermediário", 300, "Lua; planetas", "não classificar", 2150.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("andromeda_telescopios_newtoniano_180_mm", "Andrômeda Telescópios", "Newtoniano 180 mm", 180, 1200, 6.67, "Newtoniano", "manual / motorização opcional", "intermediário", 360, "Lua; planetas; céu profundo", "potencial", 2650.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("andromeda_telescopios_newtoniano_200_mm", "Andrômeda Telescópios", "Newtoniano 200 mm", 200, 1200, 6.0, "Newtoniano", "manual", "avançado", 400, "Lua; planetas; céu profundo excelente", "não classificar", 3700.0, "2026-08-12", "imagem/loja consultada"),
        TelescopeModel("andromeda_telescopios_newtoniano_380_mm", "Andrômeda Telescópios", "Newtoniano 380 mm", 380, null, null, "Newtoniano", "não confirmado", "avançado", 760, "Abertura massiva de 380mm para céu profundo", "potencial", 11200.0, "2026-08-12", "imagem/loja consultada")
    )
}
