import kotlinx.coroutines.delay

class Store(products: List<Product>) {
    private val catalog: Map<Int, Product> = products.associateBy { it.id }
    private val stock: MutableMap<Int, Int> = products.associate { it.id to 10 }.toMutableMap()

    private val customerEmails = mutableSetOf<String>()
    private val orders = mutableListOf<Order>()
    private var nextOrderId = 1

    fun allProducts(): List<Product> = catalog.values.toList()

    fun stockOf(product: Product): Int = stock[product.id] ?: 0

    fun uniqueCustomers(): Set<String> = customerEmails

    fun orderHistory(): List<Order> = orders

    fun findProducts(predicate: (Product) -> Boolean): List<Product> =
        catalog.values.filter(predicate)

    fun cartTotal(cart: Cart, priceRule: (Double) -> Double): Double =
        cart.items.entries
            .map { (product, qty) -> priceRule(product.price) * qty }
            .reduce { acc, value -> acc + value }

    suspend fun processPayment(customer: Customer, amount: Double): Boolean {
        println("  [payment] Processing %.2f for ${customer.name}...".format(amount))
        delay(500) // simulate network call
        println("  [payment] Approved")
        return true
    }

    suspend fun placeOrder(customer: Customer, cart: Cart): OrderResult {
        if (cart.isEmpty()) return OrderResult.EmptyCart

        for ((product, qty) in cart.items) {
            val available = stockOf(product)
            if (qty > available) return OrderResult.OutOfStock(product, qty, available)
        }

        val total = cartTotal(cart) { price -> customer.applyDiscount(price) }
        if (!processPayment(customer, total)) return OrderResult.EmptyCart

        cart.items.forEach { (product, qty) -> stock[product.id] = stockOf(product) - qty }
        customerEmails.add(customer.email)

        val order = Order(nextOrderId++, customer.name, cart.items.toMap(), total)
        orders.add(order)
        return OrderResult.Success(order)
    }
}
