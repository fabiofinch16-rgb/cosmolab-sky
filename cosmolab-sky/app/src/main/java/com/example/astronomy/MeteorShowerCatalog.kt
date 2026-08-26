package com.example.astronomy

/**
 * Astronomical catalog of annual meteor showers.
 * Data compiled from International Meteor Organization (IMO) and NASA Meteoroid Environment Office.
 */
object MeteorShowerCatalog {

    val showers: List<MeteorShower> = listOf(
        MeteorShower(
            id = "quadrantids",
            portugueseName = "Quadrântidas",
            internationalName = "Quadrantids (010 QUA)",
            startMonth = 12,
            startDay = 28,
            endMonth = 1,
            endDay = 12,
            peakMonth = 1,
            peakDay = 4,
            radiantConstellation = "Boieiro (Quadrans Muralis)",
            radiantRaDeg = 230.1, // 15h 20m
            radiantDecDeg = 49.5,
            peakZhr = 110,
            velocityKmS = 41,
            velocityDescription = "Moderadamente rápidos (41 km/s), produzem meteoros azulados brilhantes",
            parentBody = "Asteroide 2003 EH1 (núcleo rochoso de cometa fragmentado)",
            description = "Uma das chuvas mais intensas do ano, porém com pico extremamente estreito (dura cerca de 6 a 12 horas). Seu radiante está na constelação do Boieiro, próximo ao antigo asterismo 'Quadrans Muralis'. No hemisfério sul, o radiante nasce baixo próximo ao horizonte norte antes do amanhecer.",
            bestObservingTimeDescription = "Das 03:30 até o início do crepúsculo matutino (olhando para o horizonte Norte)"
        ),
        MeteorShower(
            id = "alpha_centaurids",
            portugueseName = "Alfa Centaurídeas",
            internationalName = "Alpha Centaurids (102 ACE)",
            startMonth = 1,
            startDay = 28,
            endMonth = 2,
            endDay = 21,
            peakMonth = 2,
            peakDay = 8,
            radiantConstellation = "Centauro",
            radiantRaDeg = 210.0, // 14h 00m
            radiantDecDeg = -59.0,
            peakZhr = 10,
            velocityKmS = 56,
            velocityDescription = "Rápidos (56 km/s), conhecidos por produzir bólidos muito brilhantes e coloridos",
            parentBody = "Cometa de longo período não catalogado",
            description = "Chuva exclusiva do hemisfério sul com radiante circumpolar ou muito alto no céu austral, próximo à estrela Alpha Centauri. Embora sua taxa horária média seja moderada, é famosa por gerar bólidos (fireballs) luminosos com trilhas duradouras.",
            bestObservingTimeDescription = "Durante toda a madrugada, com excelente posição entre 01:00 e 04:30"
        ),
        MeteorShower(
            id = "lyrids",
            portugueseName = "Líridas",
            internationalName = "Lyrids (006 LYR)",
            startMonth = 4,
            startDay = 14,
            endMonth = 4,
            endDay = 30,
            peakMonth = 4,
            peakDay = 22,
            radiantConstellation = "Lira",
            radiantRaDeg = 271.4, // 18h 05m
            radiantDecDeg = 34.0,
            peakZhr = 18,
            velocityKmS = 49,
            velocityDescription = "Rápidos (49 km/s), frequentemente deixam rastros de poeira ionizada que persistem por segundos",
            parentBody = "Cometa C/1861 G1 Thatcher",
            description = "A chuva de meteoros registrada mais antiga da história humana, com registros chineses datando de 687 a.C. Os detritos são grãos de poeira deixados pelo cometa de longo período Thatcher, que orbita o Sol a cada 415 anos.",
            bestObservingTimeDescription = "Após a meia-noite até o amanhecer, quando a constelação de Lira ganha altura a nordeste"
        ),
        MeteorShower(
            id = "eta_aquariids",
            portugueseName = "Eta Aquáridas",
            internationalName = "eta Aquariids (031 ETA)",
            startMonth = 4,
            startDay = 19,
            endMonth = 5,
            endDay = 28,
            peakMonth = 5,
            peakDay = 6,
            radiantConstellation = "Aquário",
            radiantRaDeg = 338.0, // 22h 32m
            radiantDecDeg = -1.0,
            peakZhr = 50,
            velocityKmS = 66,
            velocityDescription = "Muito velozes (66 km/s), produzem rastros luminosos persistentes e bólidos espetaculares",
            parentBody = "Cometa 1P/Halley",
            description = "Uma das melhores chuvas do ano para observadores do hemisfério sul! Originada das partículas deixadas pelo famoso Cometa Halley ao cruzar a órbita da Terra. O radiante sobe no céu austral a partir das 02h da madrugada, proporcionando excelente campo de visão.",
            bestObservingTimeDescription = "Das 02:30 até as primeiras luzes da alvorada matutina"
        ),
        MeteorShower(
            id = "southern_delta_aquariids",
            portugueseName = "Delta Aquáridas do Sul",
            internationalName = "Southern delta Aquariids (005 SDA)",
            startMonth = 7,
            startDay = 12,
            endMonth = 8,
            endDay = 23,
            peakMonth = 7,
            peakDay = 30,
            radiantConstellation = "Aquário",
            radiantRaDeg = 339.8, // 22h 39m
            radiantDecDeg = -16.4,
            peakZhr = 25,
            velocityKmS = 41,
            velocityDescription = "Velocidade média (41 km/s), meteoros amarelados com traços nítidos",
            parentBody = "Cometa 96P/Machholz (Complexo Marsden-Kracht)",
            description = "Excelente chuva de inverno para o Brasil e todo o hemisfério sul. O radiante fica a -16° de declinação, culminando em altitudes privilegiadas (60° a 80° acima do horizonte) perto da meia-noite e ao longo da madrugada.",
            bestObservingTimeDescription = "Das 23:00 às 04:30, com ápice quando o radiante cruza o meridiano local"
        ),
        MeteorShower(
            id = "perseids",
            portugueseName = "Perseidas",
            internationalName = "Perseids (007 PER)",
            startMonth = 7,
            startDay = 17,
            endMonth = 8,
            endDay = 24,
            peakMonth = 8,
            peakDay = 12,
            radiantConstellation = "Perseu",
            radiantRaDeg = 46.2, // 03h 05m
            radiantDecDeg = 58.0,
            peakZhr = 100,
            velocityKmS = 59,
            velocityDescription = "Velozes (59 km/s), ricos em bólidos luminosos (fireballs) e trilhas verdes",
            parentBody = "Cometa 109P/Swift-Tuttle",
            description = "Uma das chuvas mais ricas e populares do calendário astronômico global. Devido ao radiante em declinação +58°, no Brasil ela é visível mais facilmente a partir do Norte e Nordeste, ou baixando até o horizonte norte nas regiões Centro-Sul e Sul poucas horas antes do amanhecer.",
            bestObservingTimeDescription = "Das 03:00 até o clarear do dia, com foco no horizonte Norte/Nordeste"
        ),
        MeteorShower(
            id = "orionids",
            portugueseName = "Oriônidas",
            internationalName = "Orionids (008 ORI)",
            startMonth = 10,
            startDay = 2,
            endMonth = 11,
            endDay = 7,
            peakMonth = 10,
            peakDay = 21,
            radiantConstellation = "Órion",
            radiantRaDeg = 94.5, // 06h 18m
            radiantDecDeg = 16.0,
            peakZhr = 20,
            velocityKmS = 66,
            velocityDescription = "Extremamente velozes (66 km/s), produzem ionização com trilhas esverdeadas que duram segundos",
            parentBody = "Cometa 1P/Halley",
            description = "A segunda passagem da Terra pelos detritos do Cometa Halley a cada ano. Com o radiante próximo à famosa constelação de Órion (vizinho a Betelgeuse), ela é perfeitamente visível em ambos os hemisférios terrestres com excelente altitude durante a primavera austral.",
            bestObservingTimeDescription = "Da meia-noite até as 04:30 da manhã"
        ),
        MeteorShower(
            id = "leonids",
            portugueseName = "Leônidas",
            internationalName = "Leonids (013 LEO)",
            startMonth = 11,
            startDay = 6,
            endMonth = 11,
            endDay = 30,
            peakMonth = 11,
            peakDay = 17,
            radiantConstellation = "Leão",
            radiantRaDeg = 152.0, // 10h 08m
            radiantDecDeg = 22.0,
            peakZhr = 15,
            velocityKmS = 71,
            velocityDescription = "Meteoros mais velozes do céu (71 km/s), brancos ou azulados com frequentes bólidos",
            parentBody = "Cometa 55P/Tempel-Tuttle",
            description = "Famosa pelas históricas tempestades de meteoros que ocorrem em ciclos de aproximadamente 33 anos. Mesmo em anos convencionais, as Leônidas proporcionam meteoros impressionantes por causa de sua incrível velocidade de entrada na atmosfera terrestre.",
            bestObservingTimeDescription = "Das 02:30 até as 05:00 da manhã, olhando em direção ao quadrante Leste/Nordeste"
        ),
        MeteorShower(
            id = "geminids",
            portugueseName = "Geminídeas",
            internationalName = "Geminids (004 GEM)",
            startMonth = 12,
            startDay = 4,
            endMonth = 12,
            endDay = 20,
            peakMonth = 12,
            peakDay = 14,
            radiantConstellation = "Gêmeos",
            radiantRaDeg = 112.5, // 07h 30m
            radiantDecDeg = 33.0,
            peakZhr = 150,
            velocityKmS = 35,
            velocityDescription = "Lentos e densos (35 km/s), intensamente coloridos (amarelos, verdes e brancos) e brilhantes",
            parentBody = "Asteroide 3200 Phaethon",
            description = "A rainha absoluta das chuvas de meteoros anuais! Ao contrário da maioria das chuvas que provêm de cometas gelados, os detritos das Geminídeas vêm de um asteroide rochoso (Phaethon), gerando meteoros mais densos que penetram mais fundo na atmosfera, brilhando intensamente por mais tempo.",
            bestObservingTimeDescription = "Das 22:30 até as 04:30, atingindo atividade máxima por volta de 02:00"
        ),
        MeteorShower(
            id = "ursids",
            portugueseName = "Úrsidas",
            internationalName = "Ursids (015 URS)",
            startMonth = 12,
            startDay = 17,
            endMonth = 12,
            endDay = 26,
            peakMonth = 12,
            peakDay = 22,
            radiantConstellation = "Ursa Menor",
            radiantRaDeg = 217.0, // 14h 28m
            radiantDecDeg = 76.0,
            peakZhr = 10,
            velocityKmS = 33,
            velocityDescription = "Velocidade moderada (33 km/s), meteoros compactos",
            parentBody = "Cometa 8P/Tuttle",
            description = "A última chuva anual oficial do calendário de inverno setentrional. Seu radiante está muito próximo ao polo celeste norte (+76°), sendo visível preferencialmente em latitudes do hemisfério norte ou no extremo norte tropical.",
            bestObservingTimeDescription = "Durante a madrugada antes do amanhecer"
        )
    )

    val allShowers: List<MeteorShower> get() = showers

    fun getById(id: String): MeteorShower? = showers.firstOrNull { it.id.equals(id, ignoreCase = true) }

    fun findShowerById(id: String): MeteorShower? = getById(id)

    fun getActiveShowers(calendar: java.util.Calendar): List<MeteorShower> {
        return showers.filter { it.isActive(calendar) }
    }
}
