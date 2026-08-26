package com.example.astronomy

object CuriosityCatalog {

    fun getCuriosityForPlanet(planet: Planet): String {
        return when (planet) {
            Planet.MERCURY -> "Mercúrio não possui atmosfera significativa para reter calor. Durante o dia, a temperatura atinge escaldantes 430°C, enquanto à noite despenca para freezing -180°C!"
            Planet.VENUS -> "Vênus gira no sentido contrário à maioria dos planetas (rotação retrógrada). Lá, o Sol nasce no oeste e se põe no leste — e um único dia venusiano dura mais que um ano inteiro no planeta!"
            Planet.MARS -> "Marte abriga o Monte Olimpo, o maior vulcão do Sistema Solar! Ele possui 22 km de altura, o que equivale a quase três vezes a altitude do Monte Everest."
            Planet.JUPITER -> "A Grande Mancha Vermelha de Júpiter é uma tempestade ciclônica gigante maior que o próprio planeta Terra, que continua soprando há mais de 300 anos!"
            Planet.SATURN -> "Saturno é o planeta menos denso de todo o Sistema Solar. Se existisse um oceano gigante o suficiente para contê-lo, Saturno flutuaria na água!"
            Planet.URANUS -> "Urano gira praticamente 'deitado' de lado, com uma inclinação de 98°. Por causa disso, cada um de seus pólos passa 42 anos seguidos sob luz solar constante e 42 anos na escuridão total!"
            Planet.NEPTUNE -> "Netuno possui os ventos mais rápidos e violentos de todo o Sistema Solar, atingindo incríveis velocidades supersônicas de até 2.100 km/h!"
        }
    }

    fun getCuriosityForDso(dso: DeepSkyObject): String {
        return when (dso.id.uppercase()) {
            "M42" -> "A Nebulosa de Órion é um berçário estelar a 1.350 anos-luz de distância. No seu centro nascem centenas de novas estrelas no famoso sistema quádruplo do Trapézio!"
            "M45" -> "Conhecidas no Japão como 'Subaru', as Plêiades são um aglomerado jovem de apenas 100 milhões de anos. As suas estrelas principais estão mergulhadas em um manto de poeira azulada de reflexão."
            "M31" -> "Andrômeda é o objeto mais distante visível a olho nu, a 2,5 milhões de anos-luz. A luz que você observa hoje saiu de lá quando os primeiros hominídeos surgiam na Terra!"
            "M27" -> "A Nebulosa do Haltere foi a primeiríssima nebulosa planetária descoberta na história humana, identificada por Charles Messier em 1764."
            "M57" -> "A Nebulosa do Anel é uma 'bolha' de gás luminescente expelida por uma estrela moribunda no seu centro, expandindo-se a mais de 20 km por segundo!"
            "M13" -> "Em 1974, o famoso radiotelescópio de Arecibo enviou uma mensagem de rádio direcionada ao Aglomerado de Hércules contendo dados sobre a humanidade e a Terra!"
            "M81" -> "M81 é uma galáxia espiral perfeita. No seu centro reside um buraco negro supermassivo com massa equivalente a 70 milhões de vezes a do nosso Sol."
            "M82" -> "M82 sofre uma formação estelar tão intensa ('starburst') devido às forças de maré com M81 que seus ventos galácticos expulsam colunas gigantescas de hidrogênio ao espaço!"
            "M8" -> "O coração da Nebulosa da Lagoa abriga o 'Tornado de Ampulheta', uma região turbulenta moldada por ventos estelares de estrelas jovens e massivas."
            "M20" -> "A Nebulosa Trífida deve seu nome às três faixas escuras de poeira que a cortam, combinando de forma única nebulosidade de emissão (vermelha) e reflexão (azul)."
            "M4" -> "M4 está localizado no centro de Escorpião e é um dos aglomerados globulares mais próximos de nós. Em seu interior foi encontrado um planeta anão com 12.7 bilhões de anos!"
            "M22" -> "M22 é um dos aglomerados globulares mais impressionantes do hemisfério sul e abriga dois buracos negros de massa estelar confirmados em seu centro!"
            "M7" -> "Conhecido desde 130 d.C., o Aglomerado de Ptolomeu foi registrado pelo astrônomo grego Ptolomeu como uma 'nuvem na cauda do escorpião'."
            "M6" -> "O padrão brilhante das estrelas do Aglomerado da Borboleta lembra as asas abertas de uma borboleta. A estrela mais destacada na ponta da asa é uma gigante alaranjada."
            "NGC4755" -> "A Caixa de Joias recebeu este nome do astrônomo John Herschel porque suas estrelas coloridas em campo parecem pedras preciosas reluzentes em um estojo."
            "NGC5139" -> "Ômega Centauri abriga cerca de 10 milhões de estrelas! Cientistas acreditam que ele seja na verdade o núcleo remanescente de uma galáxia anã devorada pela Via Láctea."
            "M16" -> "M16 abriga os icônicos 'Pilares da Criação', imensas colunas de gás e poeira interstelhar imortalizadas pelo Telescópio Espacial Hubble."
            "M17" -> "A Nebulosa Ômega possui gás e poeira suficientes para formar mais de 800 sóis. O formato suave e curvado lembra um cisne majestoso flutuando no espaço."
            "M11" -> "O Aglomerado do Pato Selvagem é um dos mais densos aglomerados abertos conhecidos, contendo cerca de 2.900 estrelas agrupadas."
            "ALBIREO" -> "Albireo é considerada a estrela dupla visualmente mais bonita do céu. No telescópio, ela se divide em uma estrela dourada/alaranjada e uma companheira azul-safira!"
            "M51" -> "A Galáxia do Redemoinho foi a primeira galáxia na história a ter sua estrutura em espiral claramente identificada, desenhada em 1845 por Lord Rosse."
            "M104" -> "A Galáxia do Sombrero possui um halo maciço contendo quase 2.000 aglomerados globulares e um núcleo ultraluminoso de emissão de Raios-X."
            "NGC253" -> "NGC 253 é uma das galáxias espirais mais empolgantes e brilhantes do céu austral, conhecida por suas densas nuvens de poeira e alta taxa de nascimento estelar."
            "M1" -> "A Nebulosa do Caranguejo é o remanescente da explosão de uma supernova testemunhada por astrônomos chineses e árabes no ano de 1054 d.C.!"
            else -> "Este objeto é uma das maravilhas catalogadas no nosso céu noturno, revelando detalhes fascinantes sobre a evolução estelar e a estrutura da galáxia!"
        }
    }
}
