package com.fyrecean.boxoffice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class BoxOfficeController(private val searchService: SearchService) {
    @GetMapping("/search")
    fun search(@RequestParam("q") query: String): List<Movie> {
        val result = searchService.getSearchResults(query)
//        searchService.saveCacheToDisk()
        return result
    }
}