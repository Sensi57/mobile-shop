enum class Category { ELECTRONICS, BOOKS, CLOTHING, HOME }

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val category: Category
)

data class Order(
    val id: Int,
    val customerName: String,
    val items: Map<Product, Int>,
    val total: Double
)

interface Discountable {
    fun applyDiscount(price: Double): Double
}

open class Customer(val name: String, val email: String) : Discountable {
    open val label: String = "Regular"

    // Regular customers get no discount
    override fun applyDiscount(price: Double): Double = price

    open fun describe(): String = "$name <$email> [$label]"
}

class PremiumCustomer(
    name: String,
    email: String,
    private val discountPercent: Int
) : Customer(name, email) {
    override val label: String = "Premium -$discountPercent%"

    override fun applyDiscount(price: Double): Double =
        price * (100 - discountPercent) / 100.0
}

class VipCustomer(name: String, email: String) : Customer(name, email) {
    override val label: String = "VIP -25% + free shipping"

    override fun applyDiscount(price: Double): Double = price * 0.75

    override fun describe(): String = "*** ${super.describe()} ***"
}

sealed class OrderResult {
    data class Success(val order: Order) : OrderResult()
    data class OutOfStock(val product: Product, val requested: Int, val available: Int) : OrderResult()
    data object EmptyCart : OrderResult()
}

class Cart {
    private val _items = mutableMapOf<Product, Int>()
    val items: Map<Product, Int> get() = _items

    fun add(product: Product, quantity: Int = 1) {
        require(quantity > 0) { "Quantity must be positive" }
        _items[product] = (_items[product] ?: 0) + quantity
    }

    fun isEmpty(): Boolean = _items.isEmpty()
}
