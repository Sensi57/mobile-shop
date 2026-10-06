import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

fun describeResult(result: OrderResult): String = when (result) {
    is OrderResult.Success ->
        "Order #${result.order.id} for ${result.order.customerName}: %.2f".format(result.order.total)
    is OrderResult.OutOfStock ->
        "Out of stock: ${result.product.name} (requested ${result.requested}, available ${result.available})"
    OrderResult.EmptyCart -> "Cart is empty"
}

fun main() = runBlocking {
    val storeName: String = "Kotlin Shop"
    val taxRate: Double = 0.12
    var processed = 0

    println("=== Welcome to $storeName ===\n")

    val store = Store(
        listOf(
            Product(1, "Laptop", 1200.0, Category.ELECTRONICS),
            Product(2, "Headphones", 150.0, Category.ELECTRONICS),
            Product(3, "Kotlin in Action", 45.0, Category.BOOKS),
            Product(4, "T-Shirt", 20.0, Category.CLOTHING),
            Product(5, "Desk Lamp", 35.0, Category.HOME),
            Product(6, "Jacket", 120.0, Category.CLOTHING)
        )
    )

    println("Catalog:")
    for (p in store.allProducts()) {
        val tag = if (p.price > 100) "premium" else "budget"
        println("  #${p.id} ${p.name} - ${p.price} (${p.category}, $tag), stock=${store.stockOf(p)}")
    }

    println("\nElectronics names: " +
        store.findProducts { it.category == Category.ELECTRONICS }.map { it.name })

    val cheap = store.findProducts { it.price < 50 }.map { it.name }
    println("Products under 50: $cheap")

    val totalCatalogValue = store.allProducts().map { it.price }.reduce { a, b -> a + b }
    println("Total catalog value: $totalCatalogValue")

    val byCategory = store.allProducts().groupBy { it.category }
    byCategory.forEach { (cat, items) -> println("  $cat -> ${items.size} item(s)") }

    val customers: List<Customer> = listOf(
        Customer("Aliya", "aliya@gmail.com"),
        PremiumCustomer("Samat", "samat@mail.com", 10),
        VipCustomer("Serik", "serik@mail.com")
    )
    println("\nCustomers:")
    customers.forEach { println("  ${it.describe()}") }

    val carts = customers.map { customer ->
        val cart = Cart()
        cart.add(store.allProducts()[0])         
        cart.add(store.allProducts()[2], 2)      
        customer to cart
    }.toMutableList()

    carts.add(Customer("Ali", "ali@mail.com") to Cart())
    carts.add(Customer("Daniya", "abirkbekov@mail.com") to Cart().apply { add(store.allProducts()[3], 50) })

    println("\nProcessing orders concurrently...")
    val results = carts.map { (customer, cart) ->
        async { store.placeOrder(customer, cart) }
    }.awaitAll()

    println("\nResults:")
    results.forEach {
        println("  ${describeResult(it)}")
        processed++
    }

    val successful = results.filterIsInstance<OrderResult.Success>().map { it.order }
    val revenue = successful.map { it.total }.fold(0.0) { acc, t -> acc + t }
    println("\nProcessed $processed requests, ${successful.size} successful")
    println("Revenue: %.2f (with tax %.2f)".format(revenue, revenue * (1 + taxRate)))
    println("Unique customers: ${store.uniqueCustomers()}")
    println("Stock left for Laptop: ${store.stockOf(store.allProducts()[0])}")
}
