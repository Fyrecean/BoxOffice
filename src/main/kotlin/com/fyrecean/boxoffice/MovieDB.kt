package com.fyrecean.boxoffice

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.context.annotation.Configuration

@Configuration
class MovieDB {
    private val httpClient = OkHttpClient()
    private val movieDBAPIKey = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1MjE0ZmI1ZGVmNGIxYTljMjI4MmM2YWFkN2I4M2ViYiIsIm5iZiI6MTcyODc2ODU5MS4xOTY4OTMsInN1YiI6IjY1ZDU1Nzg1YzhhNWFjMDE0OGUwZWY5OSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.AIhVY61hWURGpefpboU9ZZUtSg42DUA-VfnsMUn3fJo"

    class MovieDBSearchDeserializer : JsonDeserializer<List<Movie>>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<Movie> {
            val objectMapper = jacksonObjectMapper().registerKotlinModule()
            val movies = mutableListOf<Movie>()
            val node: JsonNode = p.codec.readTree(p)
            if (node.isArray) {
                for (item in node) {
                    try {
                        if (Integer.parseInt(item["vote_count"].toString()) > 100) {
                            val movie = objectMapper.treeToValue<Movie>(item)
                            movies.add(movie)
                        }
                    } catch (_: Exception) { }
                }
            }
            return movies
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MovieDBSearchResponse(
        @JsonProperty("results") @JsonDeserialize(using = MovieDBSearchDeserializer::class) val results: List<Movie>
    )

    fun search(query: String): List<Movie> {
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/search/movie?query=$query&include_adult=false")
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $movieDBAPIKey")
            .build()

        val response = httpClient.newCall(request).execute()
        val responseJsonStr = response.body?.string() ?: ""
        val objectMapper = jacksonObjectMapper().registerKotlinModule()
        val searchResponse: MovieDBSearchResponse = objectMapper.readValue(responseJsonStr)
        return searchResponse.results
    }
}