package com.fyrecean.boxoffice

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Configuration
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

@Configuration
class SearchCache() {
    data class CachedIds(
        val expirationDate: Instant,
        val movieIds: List<Int>
    )

    private var searchCache: ConcurrentHashMap<String, CachedIds> = ConcurrentHashMap()
    private var movieCache: ConcurrentHashMap<Int, Movie> = ConcurrentHashMap()

    var cacheDurationInSeconds: Long = 7*24*60*60

    fun getOrAdd(key: String, orElse: () -> List<Movie>): List<Movie> {
        if (searchCache[key] != null && searchCache[key]!!.expirationDate.isAfter(Instant.now())) {
            val result = mutableListOf<Movie>()
            for (id in searchCache[key]!!.movieIds) {
                if (movieCache[id] == null) break
                result.add(movieCache[id]!!)
            }
            return result
        } else {
            val result = orElse()
            searchCache[key] = CachedIds(
                Instant.now().plus(cacheDurationInSeconds, ChronoUnit.SECONDS),
                result.map {it.id}
            )
            for (movie in result) {
                movieCache[movie.id] = movie
            }
            return result
        }
    }

    private val objectMapper = jacksonObjectMapper().registerKotlinModule().apply {
        registerModule(JavaTimeModule())
    }

    // Paths to your cache files
    private val searchCacheFile = File("data/searchCache.json")
    private val movieCacheFile = File("data/movieCache.json")

    fun saveToDisk() {
        searchCacheFile.writeText(objectMapper.writeValueAsString(searchCache))
        movieCacheFile.writeText(objectMapper.writeValueAsString(movieCache))
    }

    fun loadFromDisk() {
        val dataDir = File("data")
        if (!dataDir.exists()) {
            dataDir.mkdir()
        } else {
            try {
                if (searchCacheFile.exists()) {
                    searchCache = objectMapper.readValue(searchCacheFile.readText())
                }
            } catch (_: Exception) {
                searchCache = ConcurrentHashMap()
            }
            try {
                if (movieCacheFile.exists()) {
                    movieCache = objectMapper.readValue(movieCacheFile.readText())
                }
            } catch (_: Exception) {
                movieCache = ConcurrentHashMap()
            }
        }
    }
}