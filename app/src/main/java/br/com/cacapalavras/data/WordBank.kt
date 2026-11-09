package br.com.cacapalavras.data

import br.com.cacapalavras.domain.WordTheme

/** Banco de palavras PT-BR (FR-008). Acentos são mantidos para exibição; a grade usa a forma normalizada. */
object WordBank {
    val themes: List<WordTheme> = listOf(
        WordTheme("animais", "Animais", "🐾", listOf(
            "Gato", "Cachorro", "Leão", "Tigre", "Elefante", "Girafa", "Zebra", "Macaco", "Coelho", "Tartaruga",
            "Jacaré", "Cavalo", "Ovelha", "Galinha", "Pato", "Coruja", "Raposa", "Lobo", "Urso", "Camelo",
            "Golfinho", "Baleia", "Tubarão", "Pinguim", "Canguru", "Esquilo", "Onça", "Capivara",
        )),
        WordTheme("frutas", "Frutas", "🍓", listOf(
            "Maçã", "Banana", "Laranja", "Uva", "Morango", "Abacaxi", "Melancia", "Manga", "Goiaba", "Caju",
            "Pera", "Limão", "Kiwi", "Mamão", "Acerola", "Cereja", "Ameixa", "Pêssego", "Jabuticaba", "Pitanga",
            "Maracujá", "Açaí", "Graviola", "Figo", "Tangerina", "Framboesa", "Coco",
        )),
        WordTheme("cores", "Cores", "🎨", listOf(
            "Vermelho", "Azul", "Verde", "Amarelo", "Roxo", "Laranja", "Rosa", "Preto", "Branco", "Cinza",
            "Marrom", "Bege", "Violeta", "Turquesa", "Dourado", "Prateado", "Lilás", "Magenta", "Ciano", "Vinho",
            "Creme", "Salmão", "Anil", "Ocre", "Carmim", "Esmeralda",
        )),
        WordTheme("paises", "Países", "🌎", listOf(
            "Brasil", "Argentina", "Chile", "Peru", "Uruguai", "Paraguai", "Bolívia", "Colômbia", "México", "Canadá",
            "Portugal", "Espanha", "França", "Itália", "Alemanha", "Japão", "China", "Índia", "Egito", "Grécia",
            "Rússia", "Austrália", "Marrocos", "Angola", "Noruega", "Irlanda", "Cuba", "Equador",
        )),
        WordTheme("profissoes", "Profissões", "👩‍⚕️", listOf(
            "Médico", "Professor", "Bombeiro", "Policial", "Advogado", "Engenheiro", "Dentista", "Padeiro",
            "Pedreiro", "Piloto", "Cozinheiro", "Enfermeiro", "Jornalista", "Arquiteto", "Músico", "Pintor",
            "Carteiro", "Motorista", "Garçom", "Barbeiro", "Veterinário", "Fotógrafo", "Ator", "Juiz",
            "Escritor", "Mecânico", "Cientista",
        )),
        WordTheme("esportes", "Esportes", "⚽", listOf(
            "Futebol", "Vôlei", "Basquete", "Tênis", "Natação", "Judô", "Surfe", "Boxe", "Ciclismo", "Atletismo",
            "Handebol", "Golfe", "Xadrez", "Remo", "Esgrima", "Rúgbi", "Skate", "Karatê", "Ginástica", "Corrida",
            "Polo", "Hipismo", "Canoagem", "Beisebol", "Badminton", "Escalada",
        )),
        WordTheme("corpo", "Corpo Humano", "🫀", listOf(
            "Cabeça", "Braço", "Perna", "Mão", "Peito", "Olho", "Nariz", "Boca", "Orelha", "Dente",
            "Coração", "Pulmão", "Fígado", "Joelho", "Cotovelo", "Ombro", "Pescoço", "Costas", "Barriga", "Dedo",
            "Unha", "Cabelo", "Língua", "Tornozelo", "Cérebro", "Estômago", "Queixo",
        )),
        WordTheme("cozinha", "Cozinha", "🍳", listOf(
            "Panela", "Frigideira", "Colher", "Garfo", "Faca", "Prato", "Copo", "Xícara", "Fogão", "Geladeira",
            "Forno", "Concha", "Peneira", "Tigela", "Avental", "Bule", "Chaleira", "Microondas", "Batedeira",
            "Ralador", "Escumadeira", "Jarra", "Pia", "Toalha", "Travessa", "Espátula",
        )),
        WordTheme("natureza", "Natureza", "🌿", listOf(
            "Árvore", "Floresta", "Rio", "Montanha", "Cachoeira", "Lago", "Praia", "Deserto", "Vulcão", "Ilha",
            "Folha", "Flor", "Semente", "Raiz", "Pedra", "Areia", "Nuvem", "Chuva", "Vento", "Arco-íris",
            "Campo", "Vale", "Caverna", "Mangue", "Savana", "Geleira", "Neblina",
        )),
        WordTheme("musica", "Música", "🎵", listOf(
            "Violão", "Guitarra", "Piano", "Bateria", "Flauta", "Violino", "Sanfona", "Pandeiro", "Cavaquinho",
            "Trompete", "Saxofone", "Harpa", "Tambor", "Melodia", "Ritmo", "Samba", "Forró", "Choro", "Frevo",
            "Maestro", "Orquestra", "Canção", "Acorde", "Partitura", "Berimbau", "Ukulele", "Coral",
        )),
        WordTheme("transportes", "Transportes", "🚲", listOf(
            "Carro", "Ônibus", "Bicicleta", "Moto", "Avião", "Navio", "Trem", "Metrô", "Barco", "Helicóptero",
            "Caminhão", "Táxi", "Patinete", "Jangada", "Canoa", "Foguete", "Bonde", "Balão", "Submarino",
            "Lancha", "Trator", "Carroça", "Teleférico", "Ambulância", "Van", "Iate",
        )),
        WordTheme("escola", "Escola", "📚", listOf(
            "Lápis", "Caneta", "Borracha", "Caderno", "Livro", "Mochila", "Régua", "Estojo", "Apontador", "Tesoura",
            "Cola", "Quadro", "Giz", "Recreio", "Aluno", "Prova", "Lição", "Diretor", "Biblioteca", "Mapa",
            "Globo", "Carteira", "Uniforme", "Agenda", "Compasso", "Dicionário",
        )),
        WordTheme("tecnologia", "Tecnologia", "💻", listOf(
            "Computador", "Teclado", "Mouse", "Monitor", "Celular", "Tablet", "Internet", "Aplicativo", "Senha",
            "Arquivo", "Pasta", "Robô", "Satélite", "Bateria", "Câmera", "Fone", "Impressora", "Servidor",
            "Software", "Código", "Algoritmo", "Rede", "Nuvem", "Pixel", "Chip", "Sensor", "Drone",
        )),
        WordTheme("cidades", "Cidades do Brasil", "🏙️", listOf(
            "Recife", "Salvador", "Fortaleza", "Manaus", "Belém", "Curitiba", "Natal", "Maceió", "Aracaju",
            "Teresina", "Goiânia", "Brasília", "Cuiabá", "Palmas", "Macapá", "Vitória", "Niterói",
            "Santos", "Campinas", "Olinda", "Gramado", "Paraty", "Petrópolis", "Londrina", "Joinville", "Ilhéus",
        )),
        WordTheme("clima", "Clima e Tempo", "⛅", listOf(
            "Sol", "Chuva", "Neve", "Granizo", "Trovão", "Relâmpago", "Tempestade", "Furacão", "Tornado", "Brisa",
            "Garoa", "Orvalho", "Geada", "Calor", "Frio", "Umidade", "Seca", "Nevasca", "Ciclone", "Verão",
            "Inverno", "Outono", "Primavera", "Monção", "Mormaço", "Aurora",
        )),
        WordTheme("casa", "Casa", "🏠", listOf(
            "Sala", "Quarto", "Banheiro", "Cozinha", "Varanda", "Telhado", "Janela", "Porta", "Escada", "Jardim",
            "Garagem", "Sofá", "Cama", "Mesa", "Cadeira", "Armário", "Tapete", "Cortina", "Espelho", "Lâmpada",
            "Quintal", "Sótão", "Porão", "Chuveiro", "Estante", "Almofada", "Corredor",
        )),
        WordTheme("roupas", "Roupas", "👕", listOf(
            "Camisa", "Calça", "Saia", "Vestido", "Casaco", "Jaqueta", "Meia", "Sapato", "Tênis", "Chinelo",
            "Boné", "Chapéu", "Cachecol", "Luva", "Bermuda", "Pijama", "Gravata", "Cinto", "Blusa", "Bota",
            "Sandália", "Moletom", "Colete", "Biquíni", "Sunga", "Macacão", "Suéter",
        )),
        WordTheme("flores", "Flores", "🌸", listOf(
            "Rosa", "Tulipa", "Girassol", "Orquídea", "Margarida", "Lírio", "Violeta", "Hortênsia", "Cravo",
            "Jasmim", "Lavanda", "Begônia", "Azaleia", "Camélia", "Dália", "Gérbera", "Lótus", "Magnólia",
            "Petúnia", "Bromélia", "Copo-de-leite", "Amarílis", "Gardênia", "Hibisco", "Ipê", "Papoula",
        )),
        WordTheme("mar", "Fundo do Mar", "🐠", listOf(
            "Peixe", "Polvo", "Lula", "Coral", "Estrela", "Mexilhão", "Água-viva", "Ostra", "Concha",
            "Caranguejo", "Lagosta", "Camarão", "Arraia", "Foca", "Alga", "Onda", "Maré", "Recife", "Mergulho",
            "Tesouro", "Âncora", "Sereia", "Naufrágio", "Baleia", "Plâncton", "Moreia", "Ouriço",
        )),
        WordTheme("comidas", "Comidas Brasileiras", "🍲", listOf(
            "Feijoada", "Coxinha", "Pastel", "Brigadeiro", "Tapioca", "Acarajé", "Moqueca", "Farofa", "Pamonha",
            "Quindim", "Cocada", "Canjica", "Paçoca", "Vatapá", "Churrasco", "Esfirra", "Mandioca", "Polenta",
            "Cuscuz", "Beijinho", "Bolinho", "Empada", "Baião", "Tutu", "Tacacá", "Rapadura", "Goiabada",
        )),
    )

    fun byId(id: String): WordTheme = themes.first { it.id == id }
}
