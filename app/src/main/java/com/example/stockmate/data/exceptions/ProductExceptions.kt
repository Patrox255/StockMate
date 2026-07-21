package com.example.stockmate.data.exceptions

class NoProductsFoundException(query: String) : Exception("No products found for query: $query")