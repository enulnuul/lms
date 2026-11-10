
"use strict";

// ETERNITY LIBRARY MANAGEMENT SYSTEM
// Frontend functionality and API integration.

const $ = id => document.getElementById(id);

const pages = [
  "home",
  "search",
  "validate",
  "borrow",
  "return",
  "receipt",
  "login",
  "signup",
  "otp"
];

let booksLoaded = false;
let searchRequestId = 0;

// NAVIGATION

function showPage(name) {
  const page = pages.includes(name) ? name : "home";

  document.querySelectorAll(".page").forEach(section => {
    section.classList.toggle(
      "active",
      section.id === "page-" + page
    );
  });

  document.querySelectorAll(".nav-link").forEach(button => {
    button.classList.toggle(
      "active",
      button.dataset.page === page
    );
  });

  $("navigation").classList.remove("open");
  $("menuBtn").setAttribute("aria-expanded", "false");

  if (location.hash !== "#" + page) {
    location.hash = page;
  }

  window.scrollTo(0, 0);

  if (page === "search" && !booksLoaded) {
    loadBooks();
  }
}

document.querySelectorAll("[data-page]").forEach(button => {
  button.addEventListener("click", () => {
    showPage(button.dataset.page);
  });
});

$("menuBtn").addEventListener("click", () => {
  const open = $("navigation").classList.toggle("open");

  $("menuBtn").setAttribute(
    "aria-expanded",
    String(open)
  );
});

window.addEventListener("hashchange", () => {
  showPage(location.hash.slice(1));
});

// SAFE HTML HELPERS

function makeElement(tag, className, value) {
  const element = document.createElement(tag);

  if (className) {
    element.className = className;
  }

  if (value !== undefined && value !== null) {
    element.textContent = String(value);
  }

  return element;
}

function showError(target, message) {
  target.replaceChildren(
    makeElement("div", "error-box", message)
  );
}

function showEmpty(target, message) {
  const box = makeElement("div", "empty-state");

  box.append(
    makeElement("span", "empty-icon", "⌕"),
    makeElement("p", "", message)
  );

  target.replaceChildren(box);
}

function showDetails(target, heading, rows) {
  const card = makeElement("div", "result-card");

  card.append(
    makeElement("h2", "", heading)
  );

  rows.forEach(([label, value]) => {
    const row = makeElement("div", "info-row");

    row.append(
      makeElement("span", "", label),
      makeElement("span", "", value ?? "—")
    );

    card.append(row);
  });

  target.replaceChildren(card);
}

function showToast(message) {
  const target = $("toast");

  target.textContent = message;
  target.hidden = false;

  clearTimeout(showToast.timer);

  showToast.timer = setTimeout(() => {
    target.hidden = true;
  }, 3500);
}

// API COMMUNICATION

async function api(path, options = {}) {
  let response;

  try {
    response = await fetch(path, {
      credentials: "same-origin",
      ...options,
      headers: {
        Accept: "application/json",
        ...(options.body
          ? { "Content-Type": "application/json" }
          : {}),
        ...(options.headers || {})
      }
    });
  } catch {
    throw new Error(
      "Unable to connect to the library server. " +
      "Please make sure Spring Boot is running."
    );
  }

  const contentType =
    response.headers.get("content-type") || "";

  if (!contentType.includes("application/json")) {
    throw new Error(
      "Unexpected server response. " +
      "Open the website through http://localhost:8080/."
    );
  }

  let data;

  try {
    data = await response.json();
  } catch {
    throw new Error("Unable to read the server response.");
  }

  if (!response.ok) {
    throw new Error(
      data.message ||
      data.error ||
      "Request failed (" + response.status + ")."
    );
  }

  return data;
}

// BOOK ID INPUT CHECK

function validBookId(value) {
  // Use the existing 20-character format.
  return /^BK[0-9]{18}$/.test(value);
}

// BOOK CARDS

function createBookCard(book) {
  const card = makeElement("article", "book-card");

  const availableCopies =
    Number(book.availableCopies ?? 0);

  const available = availableCopies > 0;

  card.append(
    makeElement("h3", "", book.title ?? "Untitled"),
    makeElement("p", "", "Author: " + (book.author ?? "—")),
    makeElement("p", "", "ISBN: " + (book.isbn ?? "—")),
    makeElement("p", "", "Book ID: " + (book.bookId ?? "—"))
  );

  const tag = makeElement(
    "span",
    "availability" + (available ? "" : " unavailable"),
    available
      ? availableCopies + " available"
      : "Unavailable"
  );

  card.append(tag);

  const actions = makeElement("div", "book-actions");

  const validateButton = makeElement(
    "button",
    "btn btn-outline",
    "Check ID"
  );

  validateButton.type = "button";

  validateButton.addEventListener("click", () => {
    $("validateId").value = book.bookId ?? "";
    showPage("validate");
    $("validateForm").requestSubmit();
  });

  const borrowButton = makeElement(
    "button",
    "btn btn-green",
    "Borrow"
  );

  borrowButton.type = "button";
  borrowButton.disabled = !available;

  borrowButton.addEventListener("click", () => {
    $("borrowId").value = book.bookId ?? "";
    showPage("borrow");
  });

  actions.append(validateButton, borrowButton);
  card.append(actions);

  return card;
}

// FR1 - BOOK SEARCH

async function loadBooks() {
  const requestId = ++searchRequestId;

  const type = $("searchType").value;
  const term = $("searchTerm").value.trim();

  const target = $("searchResults");
  const counter = $("resultCount");
  const heading = $("resultsTitle");

  if (term.length > 100) {
    showError(
      target,
      "Search term must not exceed 100 characters."
    );
    return;
  }

  const browseAll = term === "";

  heading.textContent = browseAll
    ? "Available Books"
    : "Search Results";

  counter.textContent = "Loading...";

  showEmpty(target, "Loading books...");

  try {
    let path;

    if (browseAll) {
      // Requires backend support for an empty search.
      path = "/api/books/search";
    } else {
      const params = new URLSearchParams({
        [type]: term
      });

      path = "/api/books/search?" + params.toString();
    }

    const books = await api(path);

    // Ignore an older response if a newer search was made.
    if (requestId !== searchRequestId) return;

    if (!Array.isArray(books)) {
      throw new Error("Invalid book search response.");
    }

    const results = browseAll
      ? books.filter(
          book => Number(book.availableCopies) > 0
        )
      : books;

    target.replaceChildren();

    counter.textContent =
      results.length + " book" +
      (results.length === 1 ? "" : "s");

    if (results.length === 0) {
      showEmpty(
        target,
        browseAll
          ? "No books are currently available."
          : "No books match your search."
      );
    } else {
      results.forEach(book => {
        target.append(createBookCard(book));
      });
    }

    booksLoaded = true;

  } catch (error) {
    if (requestId !== searchRequestId) return;

    booksLoaded = false;
    counter.textContent = "Unable to load books";
    showError(target, error.message);
  }
}

$("searchForm").addEventListener("submit", event => {
  event.preventDefault();
  loadBooks();
});

// FR2 - BOOK ID VALIDATION

$("validateForm").addEventListener("submit", async event => {
  event.preventDefault();

  const bookId = $("validateId").value.trim();
  const target = $("validateResult");

  if (!validBookId(bookId)) {
    showError(
      target,
      "Invalid Book ID. Enter BK followed by 18 digits."
    );
    return;
  }

  try {
    const data = await api(
      "/api/books/" +
      encodeURIComponent(bookId) +
      "/validate"
    );

    showDetails(target, "Validation Result", [
      ["Book ID", data.bookId],
      ["Book Title", data.title],
      ["Valid", data.valid ? "Yes" : "No"],
      ["Available Copies", data.availableCopies],
      ["Status", data.message]
    ]);

  } catch (error) {
    showError(target, error.message);
  }
});

// LOGIN FORM
// Backend authentication is not yet implemented.

$("loginForm").addEventListener("submit", event => {
  event.preventDefault();

  const email = $("loginEmail").value.trim();
  const password = $("loginPassword").value;
  const target = $("loginMessage");

  target.replaceChildren();

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!emailPattern.test(email)) {
    showError(target, "Please enter a valid email address.");
    return;
  }

  if (!password) {
    showError(target, "Please enter your password.");
    return;
  }

  showError(
    target,
    "Login is not available yet. " +
    "The authentication service has not been connected."
  );
});

// SIGN UP FORM
// Frontend validation only.
// No account is created in the database yet.

$("signupForm").addEventListener("submit", event => {
  event.preventDefault();

  const name = $("signupName").value.trim();
  const email = $("signupEmail").value.trim();
  const password = $("signupPassword").value;
  const confirm = $("signupConfirm").value;

  const target = $("signupMessage");

  target.replaceChildren();

  if (name.length < 2 || name.length > 100) {
    showError(
      target,
      "Full name must contain between 2 and 100 characters."
    );
    return;
  }

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!emailPattern.test(email) || email.length > 100) {
    showError(
      target,
      "Please enter a valid email address."
    );
    return;
  }

  if (password.length < 12 || password.length > 128) {
    showError(
      target,
      "Password must contain between 12 and 128 characters."
    );
    return;
  }

  if (password !== confirm) {
    showError(
      target,
      "Password and confirmation password do not match."
    );
    return;
  }

  showError(
    target,
    "Account registration is not available yet. " +
    "The registration service has not been connected."
  );
});

// OTP VERIFICATION
// Only the backend can generate and verify real OTPs.

$("otpForm").addEventListener("submit", event => {
  event.preventDefault();

  const code = $("otpCode").value.trim();
  const target = $("otpMessage");

  target.replaceChildren();

  if (!/^\d{6}$/.test(code)) {
    showError(
      target,
      "Please enter a valid six-digit verification code."
    );
    return;
  }

  showError(
    target,
    "OTP verification is not available yet. " +
    "No code has been verified."
  );
});

// BORROWING AND RETURNING
// These actions require real authentication and authorization.
// Buttons stay disabled until the secure backend is integrated.

$("borrowForm").addEventListener("submit", event => {
  event.preventDefault();

  showError(
    $("borrowResult"),
    "Please complete login and OTP verification before borrowing."
  );
});

$("returnForm").addEventListener("submit", event => {
  event.preventDefault();

  showError(
    $("returnResult"),
    "Please complete login and OTP verification before returning."
  );
});

// BACKEND CONNECTION STATUS

async function checkBackend() {
  try {
    // A non-empty search works with the original backend.
    await api("/api/books/search?title=Clean");

    $("apiStatus").textContent = "Backend connected";
  } catch {
    $("apiStatus").textContent = "Backend unavailable";
  }
}

// INITIALIZE WEBSITE

showPage(location.hash.slice(1) || "home");
checkBackend();
