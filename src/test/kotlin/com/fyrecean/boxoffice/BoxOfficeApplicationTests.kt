package com.fyrecean.boxoffice

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.annotation.MergedAnnotations.Search
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
class BoxOfficeApplicationTests {
	@Test
	fun movieDBParsing() {
		val searchService = MovieDB()
		val searchResults = searchService.search("annihilation")
		val annihilation = searchResults.find{it.id == 300668}
		assertNotNull(annihilation, "Annihilation is among search results")
		assertEquals(2018, annihilation.year, "Year parsing")
	}

	fun getTestMovieList(n: Int): List<Movie> {
		assert(n < 26) // make sure we have enough characters
		val chars = ('a'..'z').toList()
		val movies = mutableListOf<Movie>()
		for (i in 1..n) {
			val title = chars[i].toString()
			movies.add(Movie(
				id = i,
				title = title,
				description = "desc for $title",
				posterPath = "/path/$title",
				year = 2000 + i
			))
		}
		return movies
	}

	@Test
	fun `search retrieves result`() {
		val movieDBMock = mock<MovieDB>()
		whenever(movieDBMock.search("asdf")).thenReturn(
			getTestMovieList(1)
		)
		val searchService = SearchService(movieDBMock, SearchCache())
		val results = searchService.getSearchResults("asdf")
		assertNotNull(results.find{it.id == 1})
		verify(movieDBMock, times(1)).search("asdf")
	}

	@Test
	fun `cache is hit on subsequent call`() {
		val movieDBMock = mock<MovieDB>()
		whenever(movieDBMock.search("asdf")).thenReturn(
			getTestMovieList(1)
		)
		val searchService = SearchService(movieDBMock, SearchCache())
		var results = searchService.getSearchResults("asdf")
		assertNotNull(results.find{it.id == 1}, "First call found 1")

		whenever(movieDBMock.search("asdf")).thenReturn(
			getTestMovieList(2)
		)

		results = searchService.getSearchResults("asdf")
		assertNotNull(results.find{it.id == 1}, "Second call found 1")
		assertEquals(1, results.count(), "Second call didn't get newly released movie")
		verify(movieDBMock, times(1)).search("asdf")
	}

	@Test
	fun cacheInvalidation() {
		val movieDBMock = mock<MovieDB>()
		whenever(movieDBMock.search("asdf")).thenReturn(
			getTestMovieList(1)
		)
		val cache = SearchCache()
		cache.cacheDurationInSeconds = -1
		val searchService = SearchService(movieDBMock, cache)
		var results = searchService.getSearchResults("asdf")
		assertNotNull(results.find{it.id == 1}, "First call found 1")

		whenever(movieDBMock.search("asdf")).thenReturn(
			getTestMovieList(2)
		)

		results = searchService.getSearchResults("asdf")
		assertNotNull(results.find{it.id == 1}, "Second call found 1")
		assertNotNull(results.find{it.id == 2}, "Second call found 2")
		verify(movieDBMock, times(2)).search("asdf")
	}
}
