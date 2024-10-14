package com.fyrecean.boxoffice

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDate

class ReleaseYearDeserializer : JsonDeserializer<Int>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Int {
        val dateStr = p.text
        if (dateStr.length == 4) return dateStr.toInt()
        val releaseYear = LocalDate.parse(dateStr).year
        return releaseYear
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class Movie(
    @set:JsonProperty("id") var id: Int,
    @set:JsonProperty("title") var title: String,
    @set:JsonProperty("overview") var description: String,
    @set:JsonProperty("poster_path") var posterPath: String,
    @set:JsonProperty("release_date") @set:JsonDeserialize(using = ReleaseYearDeserializer::class) var year: Int
)
