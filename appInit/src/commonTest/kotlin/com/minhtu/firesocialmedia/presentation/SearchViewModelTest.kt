package com.minhtu.firesocialmedia.presentation

import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchViewModelTest {

    @Test
    fun `updateQuery updates state with latest input`() {
        val vm = SearchViewModel()

        vm.updateQuery("alice")
        assertEquals("alice", vm.query)

        vm.updateQuery("bob")
        assertEquals("bob", vm.query)
    }

    @Test
    fun `updateQuery allows empty value for clear action`() {
        val vm = SearchViewModel()

        vm.updateQuery("group")
        vm.updateQuery("")

        assertEquals("", vm.query)
    }

    @Test
    fun `initial query is empty`() {
        val vm = SearchViewModel()

        assertEquals("", vm.query)
    }
}
