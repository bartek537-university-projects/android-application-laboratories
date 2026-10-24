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

interface ReaderRepository {
    fun insert(reader: Reader)
    fun getAll(): List<Reader>
    fun getById(readerId: ReaderId): Reader?

    fun existsById(id: ReaderId): Boolean
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

sealed interface BookError : LibraryError {
    val book: Book
}

sealed interface ReaderError : LibraryError {
    val reader: Reader
}

class BookNotRegisteredError(override val book: Book) : BookError
class BookNotRentedError(override val book: Book) : BookError
class BookAlreadyRentedError(override val book: Book) : BookError
class ReaderNotRegisteredError(override val reader: Reader) : ReaderError
class TooManyActiveRentalsForReaderError(override val reader: Reader) : ReaderError

sealed interface Result<out S, out F : LibraryError> {
    data class Success<out S>(val value: S) : Result<S, Nothing>
    data class Failure<out F : LibraryError>(val failure: F) : Result<Nothing, F>
}

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

    val a = Book(title = "Animal Farm", author = "George Orwell")
    val b = a.copy(id = BookId(value = Uuid.random())) // Kolejny egzemplarz tej samej książki.
    val c = Book(title = "1984", author = "George Orwell")
    val d = Book(title = "Fables for Robots", author = "Stanisław Lem")

    library.registerBook(a)
    library.registerBook(b)
    library.registerBook(c)
    // Ostatnia książka nie została na razie celowo zarejestrowana.

    println(library.books)
    // [Book(id=BookId(value=f447750a-0a56-4828-89fe-125015f023f7), title=Animal Farm, author=George Orwell),
    //  Book(id=BookId(value=bb7482f2-d50a-4df1-912b-5ec809fe1d26), title=Animal Farm, author=George Orwell),
    //  Book(id=BookId(value=815bb7d0-fee2-4cd5-bfb4-8b6cd1fb5618), title=1984, author=George Orwell)]

    val p = Reader(firstName = "Toby", lastName = "Jones")
    val q = Reader(firstName = "Martha", lastName = "Smith")
    val r = Reader(firstName = "Jacob", lastName = "Smith")
    val s = Reader(firstName = "Hans", lastName = "Jackson")

    library.registerReader(p)
    library.registerReader(q)
    library.registerReader(r)
    // Ostatni czytelnik nie został na razie celowo zarejestrowany.

    println(library.readers)
    // [Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //  Reader(id=ReaderId(value=f13988ad-e230-4570-8efc-f48a4c3433f9), firstName=Martha, lastName=Smith),
    //  Reader(id=ReaderId(value=6482ee3f-0b40-404e-be64-8e73a8f1ea72), firstName=Jacob, lastName=Smith)]

    println(library.searchBooks("Farm"))
    // [Book(id=BookId(value=f447750a-0a56-4828-89fe-125015f023f7), title=Animal Farm, author=George Orwell),
    //  Book(id=BookId(value=bb7482f2-d50a-4df1-912b-5ec809fe1d26), title=Animal Farm, author=George Orwell)]
    println(library.searchBooks("1984"))
    // [Book(id=BookId(value=815bb7d0-fee2-4cd5-bfb4-8b6cd1fb5618), title=1984, author=George Orwell)]

    // Wypożyczanie książek.
    // a.
    println(library.rentBook(a, p)) // Success
    println(library.rentBook(d, p)) // Failure BookNotRegisteredError; Książka D nie została zarejestrowana.
    println(library.rentBook(b, s)) // Failure ReaderNotRegisteredError; Czytelnik S nie został zarejestrowany.
    // b.
    println(library.rentBook(a, q)) // Failure BookAlreadyRentedError; Książka jest już wypożyczona.
    // c.
    library.registerBook(d) // Rejestrujemy czwartą książkę.
    println(library.rentBook(b, p)) // Success
    println(library.rentBook(c, p)) // Success
    println(library.rentBook(d, p)) // Failure TooManyActiveRentalsForReaderError; Użytkownik wypożyczył już 3 książki.

    // Zwracanie książek.
    // a.
    println(library.returnBook(a)) // Success
    // b.
    println(library.returnBook(a)) // Failure BookNotRentedError

    // Historia.
    println(library.rentBook(d, p)) // Success
    // a.
    println(library.getActiveRental(a))
    // null
    println(library.getActiveRental(d))
    // Rental(id=RentalId(value=208f6d7f-0d3d-4e96-b41c-5c5abfee16f4),
    //        book=Book(id=BookId(value=3fa4584e-d7e7-4724-8629-690d5adfeae7), title=Fables for Robots, author=Stanisław Lem),
    //        reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //        rentedAt=2026-10-24T16:57:34.342310Z, returnedAt=null)
    println(library.getActiveRentals(p))
    // [Rental(id=RentalId(value=4aed820d-6605-4997-9896-118595a46cfa),
    //         book=Book(id=BookId(value=bb7482f2-d50a-4df1-912b-5ec809fe1d26), title=Animal Farm, author=George Orwell),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.341826Z, returnedAt=null),
    //  Rental(id=RentalId(value=51fd7f6c-947a-4df6-8bc6-445f16626dbb),
    //         book=Book(id=BookId(value=815bb7d0-fee2-4cd5-bfb4-8b6cd1fb5618), title=1984, author=George Orwell),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.341900Z, returnedAt=null),
    //  Rental(id=RentalId(value=208f6d7f-0d3d-4e96-b41c-5c5abfee16f4),
    //         book=Book(id=BookId(value=3fa4584e-d7e7-4724-8629-690d5adfeae7), title=Fables for Robots, author=Stanisław Lem),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.342310Z, returnedAt=null)]
    // b.
    println(library.rentals)
    // [Rental(id=RentalId(value=aeb0cd56-c324-4a3d-80d7-56c454c98c8b),
    //         book=Book(id=BookId(value=f447750a-0a56-4828-89fe-125015f023f7), title=Animal Farm, author=George Orwell),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.337392Z, returnedAt=2026-10-24T16:57:34.342115Z),
    //  Rental(id=RentalId(value=4aed820d-6605-4997-9896-118595a46cfa),
    //         book=Book(id=BookId(value=bb7482f2-d50a-4df1-912b-5ec809fe1d26), title=Animal Farm, author=George Orwell),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.341826Z, returnedAt=null),
    //  Rental(id=RentalId(value=51fd7f6c-947a-4df6-8bc6-445f16626dbb),
    //         book=Book(id=BookId(value=815bb7d0-fee2-4cd5-bfb4-8b6cd1fb5618), title=1984, author=George Orwell),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.341900Z, returnedAt=null),
    //  Rental(id=RentalId(value=208f6d7f-0d3d-4e96-b41c-5c5abfee16f4),
    //         book=Book(id=BookId(value=3fa4584e-d7e7-4724-8629-690d5adfeae7), title=Fables for Robots, author=Stanisław Lem),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.342310Z, returnedAt=null)]
    println(library.getRentalHistory(a))
    // [Rental(id=RentalId(value=aeb0cd56-c324-4a3d-80d7-56c454c98c8b),
    //         book=Book(id=BookId(value=f447750a-0a56-4828-89fe-125015f023f7), title=Animal Farm, author=George Orwell),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.337392Z, returnedAt=2026-10-24T16:57:34.342115Z)]
    println(library.getRentalHistory(p))
    // [Rental(id=RentalId(value=aeb0cd56-c324-4a3d-80d7-56c454c98c8b),
    //         book=Book(id=BookId(value=f447750a-0a56-4828-89fe-125015f023f7), title=Animal Farm, author=George Orwell),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.337392Z, returnedAt=2026-10-24T16:57:34.342115Z),
    //  Rental(id=RentalId(value=4aed820d-6605-4997-9896-118595a46cfa),
    //         book=Book(id=BookId(value=bb7482f2-d50a-4df1-912b-5ec809fe1d26), title=Animal Farm, author=George Orwell),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.341826Z, returnedAt=null),
    //  Rental(id=RentalId(value=51fd7f6c-947a-4df6-8bc6-445f16626dbb),
    //         book=Book(id=BookId(value=815bb7d0-fee2-4cd5-bfb4-8b6cd1fb5618), title=1984, author=George Orwell),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.341900Z, returnedAt=null),
    //  Rental(id=RentalId(value=208f6d7f-0d3d-4e96-b41c-5c5abfee16f4),
    //         book=Book(id=BookId(value=3fa4584e-d7e7-4724-8629-690d5adfeae7), title=Fables for Robots, author=Stanisław Lem),
    //         reader=Reader(id=ReaderId(value=c6864a81-7c35-4edb-a53a-3ea0821fdcae), firstName=Toby, lastName=Jones),
    //         rentedAt=2026-10-24T16:57:34.342310Z, returnedAt=null)]
}
