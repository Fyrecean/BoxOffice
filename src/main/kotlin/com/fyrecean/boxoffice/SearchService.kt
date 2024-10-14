package com.fyrecean.boxoffice

import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@EnableScheduling
class SearchService(private val movieDB: MovieDB, private val searchCache: SearchCache) {
    fun getSearchResults(query: String): List<Movie> =
        searchCache.getOrAdd(query) { movieDB.search(query) }

    @PostConstruct
    fun loadCacheFromDisk() = searchCache.loadFromDisk()
// 3600000 == 1 hour
    @Scheduled(fixedRate=3600000)
    fun saveCacheToDisk() = searchCache.saveToDisk()
}