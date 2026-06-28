-- MatMovies PostgreSQL Initialization Script

-- 1. Create Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    icon_name VARCHAR(255) NOT NULL
);

-- 2. Create Users Table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    subscription_status VARCHAR(50) DEFAULT 'Free'
);

-- 3. Create Movies Table
CREATE TABLE IF NOT EXISTS movies (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    year INT NOT NULL,
    duration VARCHAR(50) NOT NULL,
    rating FLOAT NOT NULL,
    genre VARCHAR(255) NOT NULL,
    video_url TEXT NOT NULL,
    poster_drawable_name VARCHAR(255) NOT NULL,
    is_series BOOLEAN DEFAULT FALSE,
    seasons_count INT DEFAULT 0,
    category_id INT REFERENCES categories(id) ON DELETE SET NULL
);

-- 4. Create Watchlist Table
CREATE TABLE IF NOT EXISTS watchlists (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) REFERENCES users(id) ON DELETE CASCADE,
    movie_id INT REFERENCES movies(id) ON DELETE CASCADE
);

-- 5. Create Watch History Table
CREATE TABLE IF NOT EXISTS watch_histories (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) REFERENCES users(id) ON DELETE CASCADE,
    movie_id INT REFERENCES movies(id) ON DELETE CASCADE,
    progress_ms INT DEFAULT 0,
    duration_ms INT DEFAULT 0,
    last_watched TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Insert Default Categories
INSERT INTO categories (name, description, icon_name) VALUES
('Trending', 'Most watched movies of the week', 'trending_up'),
('Action', 'Thrilling action-packed adventures', 'sports_martial_arts'),
('Comedy', 'Hilarious comedy features', 'sentiment_very_satisfied'),
('Series', 'Binge-worthy shows & serials', 'tv')
ON CONFLICT (name) DO NOTHING;

-- 7. Insert Default Movie Catalog
INSERT INTO movies (title, description, year, duration, rating, genre, video_url, poster_drawable_name, is_series, seasons_count, category_id) VALUES
('Bachelor Point - Season 4', 'A hilarious look into the lives of four bachelors living in Dhaka. Drama, arguments, and romance unfold.', 2023, 'Series (4 Seasons)', 4.8, 'Comedy, Drama', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 'img_bachelor_point', TRUE, 4, 4),
('Mirzapur - Season 3', 'The throne of Mirzapur lies vacant. Guddu Pandit rules with an iron fist, while Kaleen Bhaiya plans his brutal revenge.', 2024, 'Series (3 Seasons)', 4.9, 'Action, Thriller, Crime', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 'img_mirzapur', TRUE, 3, 4),
('Sintel', 'A beautifully animated fantasy film about a lonely girl who rescues a baby dragon and goes on a quest to find it.', 2010, '15 min', 4.5, 'Fantasy, Adventure', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', 'img_sintel', FALSE, 0, 1),
('Tears of Steel', 'A sci-fi action film featuring giant robots and deep emotional regrets in a post-apocalyptic future city.', 2012, '12 min', 4.2, 'Sci-Fi, Action', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4', 'img_tears_of_steel', FALSE, 0, 2),
('Elephants Dream', 'The world’s first open-source 3D animated movie, presenting a surreal dreamscape of gears, strings, and wonders.', 2006, '11 min', 4.0, 'Surreal, Sci-Fi', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 'img_elephants_dream', FALSE, 0, 3)
ON CONFLICT DO NOTHING;
