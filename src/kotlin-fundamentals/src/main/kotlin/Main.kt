import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@JvmInline
value class BookId(val value: Uuid)

@JvmInline
value class ReaderId(val value: Uuid)

@JvmInline
value class RentalId(val value: Uuid)

data class Book(
    val id: BookId = BookId(value = Uuid.random()),
    val title: String,
    val author: String,
)

data class Reader(
    val id: ReaderId = ReaderId(value = Uuid.random()),
    val firstName: String,
    val lastName: String,
)

data class Rental(
    val id: RentalId = RentalId(value = Uuid.random()),
    val book: Book,
    val reader: Reader,
    val rentedAt: Instant,
    val returnedAt: Instant? = null
) {
    init {
        check((returnedAt == null) || (rentedAt < returnedAt)) {
            "The return date must be after the rental date."
        }
    }

    val active: Boolean
        get() = returnedAt == null
}

interface BookRepository {
    fun insert(book: Book)
    fun getAll(): List<Book>
    fun getById(bookId: BookId): Book?

    fun existsById(id: BookId): Boolean
    fun findAllByQuery(query: String): List<Book>
}

interface ReaderRepository {
    fun insert(reader: Reader)
    fun getAll(): List<Reader>
    fun getById(readerId: ReaderId): Reader?

    fun existsById(id: ReaderId): Boolean
}

interface RentalRepository {
    fun insert(rental: Rental)
    fun update(rental: Rental)
    fun getAll(): List<Rental>

    fun findAllForBookId(bookId: BookId): List<Rental>
    fun findActiveForBookId(bookId: BookId): Rental?
    fun findAllForReaderId(readerId: ReaderId): List<Rental>
    fun findActiveForReaderId(readerId: ReaderId): List<Rental>
    fun countActiveForReaderId(readerId: ReaderId): Int
}

class InMemoryBookRepository(private val locale: Locale = Locale.ROOT) : BookRepository {
    private val books = mutableMapOf<BookId, Book>()

    override fun insert(book: Book) {
        require(book.id !in books) {
            "A book with the same identifier already exists."
        }
        books[book.id] = book
    }

    override fun getAll(): List<Book> {
        return books.values.toList()
    }

    override fun getById(bookId: BookId): Book? {
        return books[bookId]
    }

    override fun existsById(id: BookId): Boolean {
        return id in books
    }

    override fun findAllByQuery(query: String) = books.values.filter { book ->
        book.title.containsIgnoreCase(query, locale) || book.author.containsIgnoreCase(query, locale)
    }

    private fun String.containsIgnoreCase(other: String, locale: Locale = Locale.ROOT): Boolean {
        return lowercase(locale).contains(other.lowercase(locale))
    }
}

class InMemoryReaderRepository : ReaderRepository {
    private val readers = mutableMapOf<ReaderId, Reader>()

    override fun insert(reader: Reader) {
        require(reader.id !in readers) {
            "A reader with the same identifier already exists."
        }
        readers[reader.id] = reader
    }

    override fun getAll(): List<Reader> {
        return readers.values.toList()
    }

    override fun getById(readerId: ReaderId): Reader? {
        return readers[readerId]
    }

    override fun existsById(id: ReaderId): Boolean {
        return id in readers
    }
}

class InMemoryRentalRepository : RentalRepository {
    private val rentals = mutableMapOf<RentalId, Rental>()

    override fun insert(rental: Rental) {
        require(rental.id !in rentals) {
            "A rental with the same identifier already exists."
        }
        rentals[rental.id] = rental
    }

    override fun update(rental: Rental) {
        require(rental.id in rentals) {
            "A rental with the specified identifier does not exist."
        }
        rentals[rental.id] = rental
    }

    override fun getAll(): List<Rental> {
        return rentals.values.toList()
    }

    override fun findAllForBookId(bookId: BookId) = rentals.values.filter {
        it.book.id == bookId
    }

    override fun findActiveForBookId(bookId: BookId) = rentals.values.firstOrNull {
        it.active && it.book.id == bookId
    }

    override fun findAllForReaderId(readerId: ReaderId) = rentals.values.filter {
        it.reader.id == readerId
    }

    override fun findActiveForReaderId(readerId: ReaderId) = rentals.values.filter {
        it.active && it.reader.id == readerId
    }

    override fun countActiveForReaderId(readerId: ReaderId) = rentals.values.count {
        it.active && it.reader.id == readerId
    }
}

sealed interface LibraryError

sealed interface Result<out S, out E : LibraryError> {
    data class Success<out S>(val value: S) : Result<S, Nothing>
    data class Failure<out E : LibraryError>(val failure: E) : Result<Nothing, E>
}

sealed interface BookError : LibraryError {
    val book: Book
}

sealed interface ReaderError : LibraryError {
    val reader: Reader
}

data class BookNotRegisteredError(override val book: Book) : BookError
data class BookNotRentedError(override val book: Book) : BookError
data class BookAlreadyRentedError(override val book: Book) : BookError
data class ReaderNotRegisteredError(override val reader: Reader) : ReaderError
data class TooManyActiveRentalsForReaderError(override val reader: Reader) : ReaderError

class Library(
    private val bookRepository: BookRepository,
    private val readerRepository: ReaderRepository,
    private val rentalRepository: RentalRepository,
    private val clock: Clock = Clock.System,
) {
    val books: List<Book>
        get() = bookRepository.getAll()

    val readers: List<Reader>
        get() = readerRepository.getAll()

    val rentals: List<Rental>
        get() = rentalRepository.getAll()

    fun registerBook(book: Book) {
        bookRepository.insert(book)
    }

    fun registerReader(reader: Reader) {
        readerRepository.insert(reader)
    }

    fun searchBooks(query: String): List<Book> {
        return bookRepository.findAllByQuery(query)
    }

    fun rentBook(book: Book, reader: Reader): Result<Rental, LibraryError> {
        if (!bookRepository.existsById(book.id)) {
            return Result.Failure(BookNotRegisteredError(book))
        }
        if (!readerRepository.existsById(reader.id)) {
            return Result.Failure(ReaderNotRegisteredError(reader))
        }
        if (rentalRepository.findActiveForBookId(book.id) != null) {
            return Result.Failure(BookAlreadyRentedError(book))
        }
        if (rentalRepository.countActiveForReaderId(reader.id) >= MAXIMUM_ACTIVE_RENTALS_PER_READER) {
            return Result.Failure(TooManyActiveRentalsForReaderError(reader))
        }

        val book = bookRepository.getById(book.id)
        val reader = readerRepository.getById(reader.id)

        check(book != null && reader != null)

        val rental = Rental(book = book, reader = reader, rentedAt = clock.now())

        rentalRepository.insert(rental)

        return Result.Success(rental)
    }

    fun returnBook(book: Book): Result<Rental, BookError> {
        if (!bookRepository.existsById(book.id)) {
            return Result.Failure(BookNotRegisteredError(book))
        }

        val originalRental = rentalRepository.findActiveForBookId(book.id)  //
            ?: return Result.Failure(BookNotRentedError(book))

        val updatedRental = originalRental.copy(returnedAt = clock.now())

        rentalRepository.update(updatedRental)
        return Result.Success(updatedRental)
    }

    fun getActiveRental(book: Book): Rental? {
        return rentalRepository.findActiveForBookId(book.id)
    }

    fun getActiveRentals(reader: Reader): List<Rental> {
        return rentalRepository.findActiveForReaderId(reader.id).sortedBy { it.rentedAt }
    }

    fun getRentalHistory(book: Book): List<Rental> {
        return rentalRepository.findAllForBookId(book.id).sortedBy { it.rentedAt }
    }

    fun getRentalHistory(reader: Reader): List<Rental> {
        return rentalRepository.findAllForReaderId(reader.id).sortedBy { it.rentedAt }
    }

    companion object {
        private const val MAXIMUM_ACTIVE_RENTALS_PER_READER = 3
    }
}

fun main() {
    val locale = Locale.ROOT

    val library = Library(
        bookRepository = InMemoryBookRepository(locale = locale),
        readerRepository = InMemoryReaderRepository(),
        rentalRepository = InMemoryRentalRepository(),
        clock = Clock.System,
    )

    // Zarządzanie księgozbiorem.
    // a.
    val a = Book(title = "Animal Farm", author = "George Orwell")
    val b = a.copy(id = BookId(value = Uuid.random())) // Kolejny egzemplarz tej samej książki.
    val c = Book(title = "Fables for Robots", author = "Stanisław Lem")
    val d = Book(title = "1984", author = "George Orwell")

    println("\nRegistering books...")
    library.registerBook(a)
    library.registerBook(b)
    library.registerBook(c)
    // Ostatnia książka nie została na razie celowo zarejestrowana.

    println("Registered ${library.books.size} books.")
    println(library.books)

    // b.
    val p = Reader(firstName = "Toby", lastName = "Jones")
    val q = Reader(firstName = "Martha", lastName = "Smith")
    val r = Reader(firstName = "Jacob", lastName = "Smith")
    val s = Reader(firstName = "Hans", lastName = "Jackson")

    println("\nRegistering readers...")
    library.registerReader(p)
    library.registerReader(q)
    library.registerReader(r)
    // Ostatni czytelnik nie został na razie celowo zarejestrowany.

    println("Registered ${library.readers.size} readers.")
    println(library.readers)

    // c.
    println("\nSearching books for phrase 'ani'...")
    println(library.searchBooks("ani"))
    println("\nSearching books for phrase 'anis'...")
    println(library.searchBooks("anis"))

    // Wypożyczanie książek.
    // a.
    println("\nRenting book a for user p...")
    println(library.rentBook(a, p)) // Success
    println("\nRenting book d for user p...")
    println(library.rentBook(d, p)) // Failure BookNotRegisteredError; Książka D nie została zarejestrowana.
    println("\nRenting book b for user s...")
    println(library.rentBook(b, s)) // Failure ReaderNotRegisteredError; Czytelnik S nie został zarejestrowany.
    // b.
    println("\nRenting book a for user q...")
    println(library.rentBook(a, q)) // Failure BookAlreadyRentedError; Książka jest już wypożyczona.
    // c.
    println("\nRenting three books for user q...")
    library.registerBook(d) // Rejestrujemy czwartą książkę.
    println(library.rentBook(b, p)) // Success
    println(library.rentBook(c, p)) // Success
    println(library.rentBook(d, p)) // Failure TooManyActiveRentalsForReaderError; Użytkownik wypożyczył już 3 książki.

    // Zwracanie książek.
    // a.
    println("\nReturning book a...")
    println(library.returnBook(a)) // Success
    // b.
    println("\nReturning book a...")
    println(library.returnBook(a)) // Failure BookNotRentedError; Książka nie jest wypożyczona.

    // Historia.
    // a.
    println("\nGetting active rentals for book a...")
    println(library.getActiveRental(a))
    println("\nGetting active rentals for book b...")
    println(library.getActiveRental(b))

    println("\nGetting active rentals for reader p...")
    println(library.getActiveRentals(p))
    println("\nGetting active rentals for reader q...")
    println(library.getActiveRentals(q))

    // b.
    println("\nGetting whole rental history...")
    println(library.rentals)

    println("\nGetting rental history for book a...")
    println(library.getRentalHistory(a))

    println("\nGetting rental history for reader p...")
    println(library.getRentalHistory(p))
}
