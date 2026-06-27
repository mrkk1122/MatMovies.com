package com.example.data.repository

import com.example.data.local.MovieDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MovieRepository(private val movieDao: MovieDao) {

    val allMovies: Flow<List<MovieEntity>> = movieDao.getAllMovies()
    val featuredMovies: Flow<List<MovieEntity>> = movieDao.getFeaturedMovies()
    val trendingMovies: Flow<List<MovieEntity>> = movieDao.getTrendingMovies()
    val latestMovies: Flow<List<MovieEntity>> = movieDao.getLatestMovies()
    val categories: Flow<List<CategoryEntity>> = movieDao.getAllCategories()

    fun searchMovies(query: String): Flow<List<MovieEntity>> = movieDao.searchMovies(query)
    fun getMoviesByGenre(genre: String): Flow<List<MovieEntity>> = movieDao.getMoviesByGenre(genre)

    suspend fun getMovieById(movieId: Int): MovieEntity? = withContext(Dispatchers.IO) {
        movieDao.getMovieById(movieId)
    }

    suspend fun insertMovie(movie: MovieEntity) = withContext(Dispatchers.IO) {
        movieDao.insertMovie(movie)
    }

    suspend fun deleteMovie(movie: MovieEntity) = withContext(Dispatchers.IO) {
        movieDao.deleteMovie(movie)
    }

    // Watchlist
    fun getWatchlist(userId: String): Flow<List<MovieEntity>> = movieDao.getWatchlistForUser(userId)

    suspend fun isMovieInWatchlist(userId: String, movieId: Int): Boolean = withContext(Dispatchers.IO) {
        movieDao.isMovieInWatchlist(userId, movieId)
    }

    suspend fun addToWatchlist(userId: String, movieId: Int) = withContext(Dispatchers.IO) {
        movieDao.addToWatchlist(WatchlistEntity(userId = userId, movieId = movieId))
    }

    suspend fun removeFromWatchlist(userId: String, movieId: Int) = withContext(Dispatchers.IO) {
        movieDao.removeFromWatchlist(userId, movieId)
    }

    // Watch History
    fun getWatchHistory(userId: String): Flow<List<MovieEntity>> = movieDao.getWatchHistoryForUser(userId)

    fun getWatchHistoryEntities(userId: String): Flow<List<WatchHistoryEntity>> = movieDao.getWatchHistoryEntitiesForUser(userId)

    suspend fun getWatchHistoryItem(userId: String, movieId: Int): WatchHistoryEntity? = withContext(Dispatchers.IO) {
        movieDao.getWatchHistoryItem(userId, movieId)
    }

    suspend fun saveWatchHistory(userId: String, movieId: Int, progressMs: Long, durationMs: Long) = withContext(Dispatchers.IO) {
        val existing = movieDao.getWatchHistoryItem(userId, movieId)
        if (existing != null) {
            movieDao.saveWatchHistory(
                existing.copy(
                    progressMs = progressMs,
                    durationMs = durationMs,
                    lastWatchedTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            movieDao.saveWatchHistory(
                WatchHistoryEntity(
                    userId = userId,
                    movieId = movieId,
                    progressMs = progressMs,
                    durationMs = durationMs
                )
            )
        }
    }

    suspend fun clearWatchHistory(userId: String) = withContext(Dispatchers.IO) {
        movieDao.clearWatchHistoryForUser(userId)
    }

    // Reviews
    fun getReviews(movieId: Int): Flow<List<ReviewEntity>> = movieDao.getReviewsForMovie(movieId)

    suspend fun addReview(userId: String, username: String, movieId: Int, rating: Int, comment: String) = withContext(Dispatchers.IO) {
        movieDao.insertReview(
            ReviewEntity(
                userId = userId,
                username = username,
                movieId = movieId,
                rating = rating,
                comment = comment
            )
        )
    }

    // Pre-populate if database is empty or has very few items
    suspend fun prepopulateDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val movieCount = movieDao.getMovieCount()
        if (movieCount < 40) {
            // Clear existing to avoid duplicates when upgrading
            movieDao.deleteAllMovies()
            movieDao.deleteAllCategories()

            // Add default categories
            val defaultCategories = listOf(
                CategoryEntity(name = "Action", description = "Thrilling fights and car chases", iconName = "local_fire_department"),
                CategoryEntity(name = "Sci-Fi", description = "Futuristic science and outer space adventures", iconName = "science"),
                CategoryEntity(name = "Fantasy", description = "Magic, ancient worlds, and mystical creatures", iconName = "auto_awesome"),
                CategoryEntity(name = "Drama", description = "Deep emotional stories and human connections", iconName = "theater_comedy")
            )
            for (cat in defaultCategories) {
                movieDao.insertCategory(cat)
            }

            // Stable public streaming URLs from Google's gtv-videos-bucket
            val videoUrls = listOf(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
            )

            // Add premium, fully working default movies
            val defaultMovies = listOf(
                MovieEntity(
                    title = "Dune Chronicles",
                    description = "In the far future, a noble family gets involved in a war for control over the galaxy's most valuable asset on a dangerous desert planet.",
                    rating = 4.9,
                    year = 2024,
                    duration = "2h 35m",
                    genre = "Sci-Fi, Fantasy",
                    posterDrawableName = "img_hero_dune",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    viewsCount = 12400,
                    isFeatured = true,
                    isTrending = true,
                    language = "English",
                    subtitlesUrl = "English (SRT)"
                ),
                MovieEntity(
                    title = "Cyberpunk Neo",
                    description = "In a glowing metropolis powered by cybernetic enhancements, a mercenary takes on a dangerous heist that could change humanity forever.",
                    rating = 4.7,
                    year = 2023,
                    duration = "2h 10m",
                    genre = "Sci-Fi, Action",
                    posterDrawableName = "img_poster_cyberpunk",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    viewsCount = 9850,
                    isFeatured = false,
                    isTrending = true,
                    isLatest = true,
                    language = "English",
                    subtitlesUrl = "English (SRT), Spanish (SRT)"
                ),
                MovieEntity(
                    title = "The Ancient Portal",
                    description = "A group of archeologists uncovers an ancient mystical gateway in a dense green forest, leading to an uncharted realm of mythical creatures.",
                    rating = 4.8,
                    year = 2025,
                    duration = "1h 55m",
                    genre = "Fantasy, Adventure",
                    posterDrawableName = "img_poster_fantasy",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    viewsCount = 8200,
                    isFeatured = false,
                    isTrending = false,
                    isLatest = true,
                    language = "English",
                    subtitlesUrl = "English, German"
                ),
                MovieEntity(
                    title = "Tears of Steel",
                    description = "A futuristic drama in which a group of soldiers and scientists must salvage a giant space mech to protect Earth from rogue robotic AI.",
                    rating = 4.5,
                    year = 2022,
                    duration = "1h 22m",
                    genre = "Sci-Fi, Drama",
                    posterDrawableName = "img_hero_dune",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    viewsCount = 15300,
                    isFeatured = true,
                    isTrending = false,
                    language = "English",
                    subtitlesUrl = "English, French"
                )
            )

            for (movie in defaultMovies) {
                movieDao.insertMovie(movie)
            }

            // Generate 64 more distinctive, high-quality movies across 4 categories
            val actionTitles = listOf(
                "Vanguard Protocol" to "An elite tactical team defends a high-tech facility against an overwhelming rogue military invasion.",
                "Bullet Storm" to "A retired detective must fight through a syndicate-controlled city to rescue his kidnapped partner.",
                "Apex Predator" to "A deep jungle rescue mission turns into a fight for survival against a highly advanced genetically-modified beast.",
                "Velocity Shift" to "An underground street racer is forced to execute a daring heist to clear his family's name.",
                "Iron Fist" to "A legendary martial artist comes out of hiding to protect his village from a brutal warlord's army.",
                "The Enforcer" to "A security specialist with a dark past takes on a dangerous assignment to protect a high-ranking witness.",
                "Rogue Agent" to "Betrayed by his own agency, a black-ops operative goes off the grid to expose a massive global conspiracy.",
                "Silent Assassin" to "An expert marksman is caught in a web of deceit when his final target turns out to be an innocent scientist.",
                "Redline Fury" to "High-speed chases and explosive encounters across the desert wasteland as survivors fight for resources.",
                "Shadow Strike" to "A covert unit is deployed behind enemy lines to dismantle a devastating cyber-weapon before it launches.",
                "The Outlaw" to "A former sheriff takes justice into his own hands when a ruthless gang takes over a peaceful frontier town.",
                "Zero Hour" to "The clock is ticking for a bomb disposal expert trapped in a skyscraper controlled by armed mercenaries.",
                "Maximum Risk" to "An extreme sports athlete is recruited for a dangerous undercover mission to infiltrate a crime syndicate.",
                "Firepower" to "A high-octane battle erupts in the city center when a routine transport mission is ambushed by heavily armed rivals.",
                "The Gladiator" to "A modern warrior participates in an illegal underground tournament to earn his freedom and avenge his brother.",
                "Double Cross" to "A master thief discovers a trap set by her partners and plans the ultimate revenge heist to get even."
            )

            val scifiTitles = listOf(
                "Chronos Paradox" to "A brilliant physicist invents a temporal device, only to face devastating consequences across multiple timelines.",
                "Nebula Horizon" to "Deep space explorers discover an ancient alien artifact orbiting a dying star, emitting a strange signal.",
                "AI Genesis" to "In a futuristic society, a newly developed supercomputer gains consciousness and starts reshaping the world.",
                "Singularity" to "A cybernetic technician discovers a hidden network of synthetic humans planning a silent revolution.",
                "Quantum Echo" to "Scientists experimenting with alternate dimensions accidentally release a cosmic entity into our world.",
                "Solaris Station" to "An isolated research crew on a distant moon must fight to survive when their life support begins to fail.",
                "Mechanized War" to "Giant piloted mechs engage in epic battles to defend the last human city from colossal alien invaders.",
                "Stellar Voyager" to "A lonely astronaut's journey to the edge of the galaxy takes a mysterious turn when he encounters a wormhole.",
                "Cyber City 2099" to "A neon-drenched detective story set in a dystopian future where human memories can be bought and sold.",
                "The Bio-Dome" to "In a barren wasteland, a self-sustaining eco-dome holds the last remnants of human life, but secrets lurk within.",
                "Dark Matter" to "An experimental warp-drive ship disappears and returns years later with no crew, but something else on board.",
                "Android Dream" to "A synthetic companion seeks to understand human emotion while escaping from a corporate reclamation unit.",
                "Vector Zero" to "A deadly nanotech virus threatens to wipe out a colony planet, and a desperate cure must be synthesized.",
                "Galaxy Alliance" to "A diverse crew of alien diplomats and human pilots unite to prevent an intergalactic war of extinction.",
                "Deep Space Nine" to "The crew of a deep space observation post detects an approaching fleet of hostile unidentified objects.",
                "The Grid" to "An engineer is pulled inside a digital reality and must win a series of deadly virtual games to escape."
            )

            val fantasyTitles = listOf(
                "Realm of Whispers" to "A young apprentice discovers she possesses the forbidden magic of communicating with ancient forest spirits.",
                "The Dragon's Legacy" to "An epic journey of an exiled prince who must find and tame the last living dragon to reclaim his kingdom.",
                "Spellbound" to "A mystical sorcerer searches for a lost grimoire that holds the key to sealing a dark underworld rift.",
                "Crown of Ashes" to "A dramatic war of succession erupts between rival kingdoms, each seeking the power of the primal elements.",
                "Lost Elixir" to "An adventurous alchemist sets off to find the legendary fountain of youth hidden deep inside a moving maze.",
                "Shadow Weaver" to "A rogue thief with the power to manipulate shadows is hired to steal a sacred artifact from the high temple.",
                "Eternal Frost" to "A brave warrior searches for the fire-stone to save her village from a curse of never-ending winter.",
                "The Golden Griffin" to "A young knight forms an unlikely bond with a mythical beast while defending the realm from dark magic.",
                "Runic Oracle" to "An ancient runic tablet is uncovered, foretelling the return of the forgotten old gods of the mountains.",
                "Chalice of Souls" to "A group of heroes embarks on a dangerous quest to destroy a cursed cup that drains life from the land.",
                "Sky Kingdom" to "Adventures on floating islands where airship captains battle sky pirates and ancient flying beasts.",
                "The Wood Nymph" to "A mysterious guardian of the sacred grove must protect the mother tree from a greedy industrial empire.",
                "Mage Academy" to "A talented student at a magic university uncovers a dark secret hidden deep in the restricted library vaults.",
                "Stone Sentinel" to "A massive stone giant awakens after centuries to guide a lost child back to her homeland.",
                "Ocean Whispers" to "A legendary sailor discovers a hidden underwater civilization populated by mystical sea dwellers.",
                "The Iron King" to "A rebellion rises against a tyrannical king who has traded his soul for magical iron armor."
            )

            val dramaTitles = listOf(
                "A Walk in the Rain" to "A touching story of two strangers who find solace and hope in each other's company during a stormy night.",
                "The Music Box" to "An old pianist recounts his life and loves through the melodies of a treasured childhood keepsake.",
                "Echoes of Silence" to "A silent drama about a family coping with loss and finding ways to rebuild their broken bonds.",
                "Midnight Symphony" to "A struggling composer gets one final chance to write a masterpiece with the help of a young violinist.",
                "Golden Leaves" to "A beautiful exploration of aging, friendship, and memories set in a countryside retirement home.",
                "The Last Chapter" to "A renowned author tries to finish his autobiography while reconciling with his estranged daughter.",
                "Finding Home" to "An orphan boy's journey across the country in search of the only family member he has left.",
                "Broken Strings" to "A talented cellist loses her hearing and must find a new way to feel and share the magic of music.",
                "The Lighthouse" to "A lonely lighthouse keeper's life is transformed when a mysterious traveler washes ashore after a storm.",
                "Unspoken Words" to "Two childhood friends meet after decades and confront the secrets that originally drove them apart.",
                "City Lights" to "An aspiring actress and a street painter support each other's dreams amidst the bustle of New York City.",
                "Before Sunrise" to "A beautifully simple conversation between two travelers on a long overnight train ride across Europe.",
                "The Canvas" to "An artist pours his soul into his final painting, which tells the secret story of a forgotten historic event.",
                "Rhythm of Life" to "A passionate dancer struggles to keep his community center open to teach underprivileged kids.",
                "Summer Breeze" to "A heartwarming coming-of-age story of three teenagers during their last summer before college.",
                "The Letter" to "A long-lost love letter from WWII is delivered eighty years later, triggering a journey of discovery."
            )

            val extraMovies = mutableListOf<MovieEntity>()
            val allExtraTitles = listOf(
                "Action" to actionTitles,
                "Sci-Fi" to scifiTitles,
                "Fantasy" to fantasyTitles,
                "Drama" to dramaTitles
            )

            var index = 5
            for ((genreName, titleList) in allExtraTitles) {
                for ((title, desc) in titleList) {
                    val rating = (4.0 + (index % 11) * 0.1).coerceAtMost(5.0)
                    val year = 2020 + (index % 6)
                    val hour = 1 + (index % 2)
                    val min = 15 + (index % 40)
                    val duration = "${hour}h ${min}m"
                    val videoUrl = videoUrls[index % videoUrls.size]
                    val viewsCount = 4500 + (index * 173) % 25000

                    extraMovies.add(
                        MovieEntity(
                            title = title,
                            description = desc,
                            rating = rating,
                            year = year,
                            duration = duration,
                            genre = genreName,
                            posterDrawableName = when (genreName) {
                                "Action" -> "img_poster_cyberpunk"
                                "Sci-Fi" -> "img_poster_cyberpunk"
                                "Fantasy" -> "img_poster_fantasy"
                                else -> "img_hero_dune"
                            },
                            videoUrl = videoUrl,
                            viewsCount = viewsCount,
                            isFeatured = (index % 9 == 0),
                            isTrending = (index % 7 == 0),
                            isLatest = (index % 5 == 0),
                            language = "English",
                            subtitlesUrl = "English (SRT)"
                        )
                    )
                    index++
                }
            }

            for (movie in extraMovies) {
                movieDao.insertMovie(movie)
            }
        }
    }
}
