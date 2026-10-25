#let info = (
  "date": datetime(day: 24, month: 10, year: 2026),
  "laboratory": 3,
  "title": "Implementacja modelu obiektowego w Kotlinie",
  "album": "315945",
  "author": "Bartosz Bieniek",
  "group": 1,
  "city": "Gliwice",
  "supervisor": "dr hab. in. Adam Zielonka, prof. PŚ",
)

#set document(title: info.title, author: info.author, date: info.date)

#set page("a4")
#set heading(numbering: "1.1.")
#set par(justify: true)
#set text(lang: "pl", size: 12pt)

#show figure: set align(start)

/**
 *
 * Strona tytułowa
 *
 */

#align(center)[
  #set text(size: 1.25em)
  #set par(leading: 1.2em)

  #upper[Politechnika Śląska]\
  Wydział Matematyki Stosowanej\
  #info.city
]

#v(3em)

#align(center)[
  #text(size: 1.6em, weight: "bold")[#upper[Sprawozdanie]]

  #text(size: 1.2em)[z przedmiotu] #v(-1em)

  #text(size: 1.8em, weight: "bold")[Aplikacje dla systemu Android]
]

#v(3em)

#align(center)[
  #divider() #v(-0.2em)

  #text(size: 0.95em)[#title()] #v(0.2em)

  #text(size: 1.3em)[Laboratorium #info.laboratory.] #v(0.5em)

  #divider()
]

#v(3em)

#align(right)[
  #block(width: 40%)[
    #set align(left)
    #set text(hyphenate: true)
    #set par(leading: 1.2em)

    *Autor:* #info.author\
    *Numer albumu:* #info.album\
    *Grupa*: #info.group\
    *Prowadzący*: #info.supervisor\
    *Data wykonania*: #info.date.display("[day].[month].[year]")
  ]
]

#v(1fr)

#align(center)[
  #info.city, #info.date.display("[year]")
]

#pagebreak()

/**
 *
 * Spis treści
 *
 */

#counter(page).update(1)

#set page(
  header: [
    Aplikacje dla systemu Android
    #h(1fr)
    Sprawozdanie
    #v(-0.75em)
    #line(length: 100%)
  ],
  numbering: "1",
)

#outline()

#pagebreak()

/**
 *
 * Treść
 *
 */

= Cel ćwiczenia.

Celem ćwiczenia było zaprojektowanie i zaimplementowanie prostego programu imitującego system biblioteczny. Aplikacja powinna umożliwiać zarządzanie książkami, czytelnikami oraz wypożyczeniami, zapisując dane w pamięci operacyjnej.

= Wykorzystane technologie.

- Język _Kotlin_ w wersji _2.4.20_
- Narzędzie _Gradle_ w wersji _9.6.0_

= Model obiektowy.

Aby odwzorować działanie systemu bibliotecznego, w pierwszej kolejności konieczne było utworzenie obiektów domenowych reprezentujące najważniejsze obiekty z perspektywy bieznesowej - książki, czytelnika oraz wypożyczenia.

== Modele domenowe.

W tym celu zdecydowałem się skorzystać z `data class`-ów, które pomogą mi zachować spójność danych. Są one z założenia niezmienne (niemutowalne), dzięki czemu możliwe jest uniknięcie sytuacji, w których obiekt po zapisaniu w kolekcji zostałby zmieniony z zewnątrz.

#figure(
  ```kotlin
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
  ```,
  caption: "Modele domenowe.",
)

Model książki zawiera podstawowe informacje o lekturze: tytuł, autora oraz identyfikator, dzięki któremu możliwe będzie rozpoznanie konkretnej kopii.
Model czytelnika zawiera podstawowe informacje, takie jak imię, nazwisko oraz identyfikator - możliwe jest przecież, aby z biblioteki mogło korzystać wiele tak samo nazywających się osób.
Model wypożyczenia oprócz identyfikatora, książki, czytelnika i daty wypożyczenia, posiada też opcjonalne pole z datą zwrotu, która zostanie uzupełniona w momencie oddania książki. Gdy wartość zostanie wskazana, warto sprawdzić, czy jest ona poprawna, a więc czy następuje po dacie wypożyczenia. Do tego celu wykorzystałem blok inicjalizujący oraz instrukcję check, która wyrzuca wyjątek `IllegalStateException` w przypadku, gdy wskazany warunek logiczny nie zostanie spełniony.

== Identyfikatory.

Pracując z wieloma obiektami korzystającymi z identyfikatorów tego samego typu (na przykład `String`, czy `Uuid`) łatwo o pomyłkę - może zdarzyć się, że przez nieostrożność będziemy wyszukiwać książki po identyfikatorze odczytanym z czytelnika. Można temu zaradzić tworząc bardzo lekkie `value class`-y, które "owijają" wskazaną wartość. Dzięki temu powstaje nowy typ, który kompilator jest w stanie sprawdzić statycznie w czasie kompilacji, a co za tym idzie, w wymienionej wyżej sytuacji zostanie zgłoszony błąd (jak porównać typy `BookId` i `ReaderId`).

#figure(
  ```kotlin
  @JvmInline
  value class BookId(val value: Uuid)

  @JvmInline
  value class ReaderId(val value: Uuid)

  @JvmInline
  value class RentalId(val value: Uuid)
  ```,
  caption: [Identyfikatory obiektów zaimplementowane z wykorzystaniem `value class`-ów.],
)

Warto zaznaczyć, że obiekty stworzone na podstawie `value class` przechowywane są przez wartość na stosie, a nie przez referencję na kopcu.

== Repozytoria.

Aplikacja będzie musiała zawierać klasę realizującą mechanizm wypożyczeń, jednakże dla zwiększenia elastyczności i ułatwienia procesu testowania rozwiązania, można wykorzystać wzorzec repozytoriów. Polega on na odwróceniu zależności - klasa uzyskująca dostęp do danych korzysta z faktycznej implementacji przez interfejs.

Pierwszym krokiem było więc utworzenie interfejsów, przez które główna klasa będzie prowadziła odczyt i zapis danych.

Teoretycznie możliwe jest, żeby w trakcie działania programu powstała kopia obiektu domenowego zawierająca użyty już wcześniej identyfikator oraz zupełnie inne dane. Przyjąłem, że będę to traktować jako edycję obiektu, a co za tym idzie, chciałbym, żeby repozytoria funkcjonowały poprawnie mimo niepasujących (nowych) danych. Spowodowało to, że wiele metod realizuje odczyt danych jedynie na podstawie identyfikatora.

#figure(
  ```kotlin
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
  ```,
  caption: "Interfejsy repozytoriów.",
)

#pagebreak()

Ponieważ nie chciałem mieszać odpowiedzialności ani powielać kodu, podjąłem decyzję o stworzeniu implementacji nieposiadających żadnej logiki biznesowej, a jedynie najprostszą walidację zapobiegającą niespójności danych.

Zgodnie z założeniami projektu, aplikacja powinna zapisywać dane wyłącznie w pamięci ulotnej, dlatego mój kod będzie wewnętrznie pracował na kolekcjach oferowanych przez język - głównie na słownikach (`Map`-ach). Zapewniają one błyskawiczny dostęp do danych oraz ułatwiają wyszukiwanie obiektów.

#figure(
  ```kotlin
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
  ```,
  caption: [Implementacja repozytorium przechowującego ksiązki.],
)

#figure(
  ```kotlin
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
  ```,
  caption: [Implementacja repozytorium przechowującego czytelników.],
)

#figure(
  ```kotlin
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
  ```,
  caption: [Implementacja repozytorium przechowującego wypożyczenia.],
)

== Klasy rezultatów

Mechanizm wypożyczeń powinien zwracać informacje o niepowodzeniu danych akcji. Ponieważ próba zwrotu niezarejestrowanej książki, czy wypożyczenia więcej niż maksymalnej ich ilości nie jest sytuacją wyjątkową (na tyle żeby wyrzucać wyjątki), zdecydowałem się stworzyć interfejs `Result`. Będzie on zawierał dwa warianty (klasy), reprezentujące sukces oraz porażkę.

Do tego zadania idealnie sprawdzi się `sealed interface`, ponieważ uniemożliwi tworzenie kolejnych implementacji, poza tymi, które zostały zdefiniowane w tym samym pliku. Takie podejście upraszcza kod, na przykład znosząc wymóg posiadania w bloku `when` gałęzi `else` (oczywiście gdy dodamy ścieżki dla `Success` i `Failure`).

#figure(
  ```kotlin
  sealed interface LibraryError

  sealed interface Result<out S, out F : LibraryError> {
      data class Success<out S>(val value: S) : Result<S, Nothing>
      data class Failure<out F : LibraryError>(val failure: F) : Result<Nothing, F>
  }
  ```,
  caption: [Definicja klasy rezultatów.],
)

Warto zwrócić uwagę na oznaczenie w interfejsie parametrów generycznych słówkiem `out`. Informuje ono kompilator, że dany typ będzie wykorzystany jedynie do zwrotu danych, a co za tym idzie możliwa jest generalizacja typu - na przykład przypisanie wartości `Result<Int, LibraryEmptyError>` do zmiennej typu `Result<Number, LibraryError>`.

Na potrzeby implementacji księgozbioru, utworzyłem także odpowiednią hierarchię błędów.

#figure(
  ```kotlin
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
  ```,
  caption: "Hierarchia błędów.",
)

== Implementacja księgozbioru

Korzystając ze wszystkich wcześniej utworzonych implementacji, możliwe jest zaprogramowanie klasy reprezentującej księgozbiór, zawierającej mechanizmy związane z tym procesem.

Niektóre z metod i getterów jedynie przekazują dalej dane otrzymane przez parametry, inne natomiast, takie jak `rentBook` i `returnBook` dodatkowo weryfikują warunki biznesowe i modyfikują dane.

Warto zwrócić uwagę na przekazanie do klasy `Library` instancji zegara `Clock`. Ułatwia to znacząco testowanie automatyczne - na czas testów wystarczy wstrzyknąć inny zegar i modyfikować wirtualny czas, zamiast oczekiwać na wybicie właściwej godziny.


#show figure: set block(breakable: true)

#figure(
  ```kotlin
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

          val originalRental = rentalRepository.findActiveForBookId(book.id)
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
  ```,
  caption: "Implementacja księgozbioru.",
)

Moja implementacja odwołuje się w dużej mierze do identyfikatorów, jednakże obiekty typu `Rental` zawierają migawki konkretnych czytelników i książek. Umożliwia to śledzenie ewentualnych zmian, które mogły nastąpić w ramach konta danego użytkownika (na przykład zmiana nazwiska). Aby zapewnić spójność danych i uniemożliwić utworzenie wypożyczenia z nieaktualnymi danymi, przed zapisem w metodzie `rentBook` pobierane są z repozytorium najświeższe dane.

#show figure: set block(breakable: false)

= Prezentacja działania aplikacji.

Przed przetestowaniem programu konieczne jest utworzenie obiektu księgozbioru, wraz ze wszystkimi niezbędnymi repozytoriami.

#figure(
  ```kotlin
  val locale = Locale.ROOT

  val library = Library(
      bookRepository = InMemoryBookRepository(locale = locale),
      readerRepository = InMemoryReaderRepository(),
      rentalRepository = InMemoryRentalRepository(),
      clock = Clock.System,
  )
  ```,
  caption: "Utworzenie obiektu biblioteki.",
)

== Zarządzanie biblioteką.

=== Dodawanie książek.

#figure(
  ```kotlin
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
  ```,
  caption: "Utworzenie i dodanie książek.",
)

#figure(
  image("pictures/1_a_books.png"),
  caption: "Utworzenie i dodanie książek.",
)

=== Rejestrowanie czytelników.

#figure(
  ```kotlin
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
  ```,
  caption: "Utworzenie i zarejestrowanie użytkowników.",
)

#figure(
  image("pictures/1_b_readers.png"),
  caption: "Utworzenie i zarejestrowanie użytkowników.",
)

=== Wyszukiwanie książek po tytule lub autorze, bez rozróżniania wielkości liter.

#figure(
  ```kotlin
  println("\nSearching books for phrase 'ani'...")
  println(library.searchBooks("ani"))
  println("\nSearching books for phrase 'anis'...")
  println(library.searchBooks("anis"))
  ```,
  caption: "Wyszukiwanie książek po tytule i autorze.",
)

#figure(
  image("pictures/1_c_search.png"),
  caption: "Wyszukiwanie książek po tytule i autorze.",
)

== Wypożyczanie książek.

=== Wypożyczenie dostępnej książki przez zarejestrowanego czytelnika.

#figure(
  ```kotlin
  println("\nRenting book a for user p...")
  println(library.rentBook(a, p)) // Ok
  println("\nRenting book d for user p...")
  println(library.rentBook(d, p)) // Błąd: Książka D nie jest zarejestrowana.
  println("\nRenting book b for user s...")
  println(library.rentBook(b, s)) // Błąd: Czytelnik S nie jest zarejestrowany.
  ```,
  caption: "Wypożyczanie dostępnych książek przez zarejestrowanych użytkowników.",
)

#figure(
  image("pictures/2_a_rent.png"),
  caption: "Wypożyczanie dostępnych książek przez zarejestrowanych użytkowników.",
)

=== Odmowa wypożyczenia książki, która jest już wypożyczona.

#figure(
  ```kotlin
  println("\nRenting book a for user q...")
  println(library.rentBook(a, q)) // Błąd: Książka A jest już wypożyczona.
  ```,
  caption: "Odmowa wypożyczenia pożyczonej książki.",
)

#figure(
  image("pictures/2_b_duplicate.png"),
  caption: "Odmowa wypożyczenia pożyczonej książki.",
)

=== Ograniczenie liczby aktywnych wypożyczeń jednego czytelnika.

Każdy użytkownik może mieć do trzech aktywnych wypożyczeń.

#figure(
  ```kotlin
  println("\nRenting three books for user q...")
  library.registerBook(d) // Rejestrujemy czwartą książkę.
  println(library.rentBook(b, p)) // Ok
  println(library.rentBook(c, p)) // Ok
  println(library.rentBook(d, p)) // Błąd: Użytkownik wypożyczył już 3 książki.
  ```,
  caption: "Ograniczenie liczby wypożyczonych książek dla czytelnika.",
)

#figure(
  image("pictures/2_c_limit.png"),
  caption: "Ograniczenie liczby wypożyczonych książek dla czytelnika.",
)

== Zwracanie książek.

=== Rejestrowanie zwrotu książki.

#figure(
  ```kotlin
  println("\nReturning book a...")
  println(library.returnBook(a)) // Ok
  ```,
  caption: "Rejestrowanie zwrotu książki.",
)

#figure(
  image("pictures/3_a_normal.png"),
  caption: "Rejestrowanie zwrotu książki.",
)

=== Odmowa zwrotu niewypożyczonej książki.

#figure(
  ```kotlin
  println("\nReturning book a...")
  println(library.returnBook(a)) // Błąd: Książka nie jest wypożyczona.
  ```,
  caption: "Odmowa zwrotu niewypożyczonej książki.",
)

#figure(
  image("pictures/3_b_availability.png"),
  caption: "Odmowa zwrotu niewypożyczonej książki.",
)

== Historia.

=== Wyświetlanie aktywnego wypożyczenia dla książki.

#figure(
  ```kotlin
  println("\nGetting active rentals for book a...")
  println(library.getActiveRental(a))
  println("\nGetting active rentals for book b...")
  println(library.getActiveRental(b))
  ```,
  caption: "Wyświetlanie aktywnego wypożyczenia dla książki.",
)

#figure(
  image("pictures/4_a_book.png"),
  caption: "Wyświetlanie aktywnego wypożyczenia dla książki.",
)

=== Wyświetlanie aktywnych wypożyczeń dla czytelnika.

#figure(
  ```kotlin
  println("\nGetting active rentals for reader p...")
  println(library.getActiveRentals(p))
  println("\nGetting active rentals for reader q...")
  println(library.getActiveRentals(q))
  ```,
  caption: "Wyświetlanie aktywnych wypożyczeń dla czytelnika.",
)

#figure(
  image("pictures/4_a_reader.png"),
  caption: "Wyświetlanie aktywnych wypożyczeń dla czytelnika.",
)

=== Wyświetlanie wszystkich dokonanych wypożyczeń.

#figure(
  ```kotlin
  println("\nGetting whole rental history...")
  println(library.rentals)
  ```,
  caption: "Wyświetlanie wszystkich dokonanych wypożyczeń.",
)

#figure(
  image("pictures/4_b_all.png"),
  caption: "Wyświetlanie wszystkich dokonanych wypożyczeń.",
)

Warto zwrócić uwagę, że zakończone wypożyczenia posiadają ustawioną końcowa datę (`returnedAt`).

=== Wyświetlanie wszystkich wypożyczeń dla książki.

#figure(
  ```kotlin
  println("\nGetting rental history for book a...")
    println(library.getRentalHistory(a))
  ```,
  caption: "Wyświetlanie wszystkich wypożyczeń dla książki.",
)

#figure(
  image("pictures/4_b_book.png"),
  caption: "Wyświetlanie wszystkich wypożyczeń dla książki.",
)

=== Wyświetlanie wszystkich wypożyczeń dla czytelnika.

#figure(
  ```kotlin
  println("\nGetting rental history for reader p...")
    println(library.getRentalHistory(p))
  ```,
  caption: "Wyświetlanie wszystkich wypożyczeń dla czytelnika.",
)

#figure(
  image("pictures/4_b_reader.png"),
  caption: "Wyświetlanie wszystkich wypożyczeń dla czytelnika.",
)

= Literatura.

#bibliography("docs.bib", full: true, title: none)

