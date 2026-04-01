let ws;
let latestCategoryDiv = null;

function initWebSocket() {
    ws = new WebSocket(document.body.dataset.wsUrl);

    ws.onopen = () => {
        console.log("WebSocket connection established");
    };

    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);

        if (data.type === "heartbeat") return;

        if (data.type === "reset") {
            if (latestCategoryDiv) latestCategoryDiv.innerHTML = "";
            return;
        }

        if (data.type === "error") {
            alert(data.message || "An error occurred");
            return;
        }

        // Handle total_results by updating the header of the latest search block
        if (data.type === "total_results") {
            if (latestCategoryDiv) {
                const header = latestCategoryDiv.closest(".search-block").querySelector("h4");
                if (header) {
                    // Replace existing "Total results:" if already present, else append
                    header.textContent = header.textContent.replace(/Total results: \d+/, '');
                    header.textContent = header.textContent.trim() + ` Total results: ${data.total_results}`;
                }
            }
            return;
        }

        appendResult(data, latestCategoryDiv);
    };

    ws.onclose = () => {
        console.log("WebSocket closed, reconnecting in 2s...");
        setTimeout(initWebSocket, 2000);
    };
}

// Function to append results to the UI
function appendResult(item, categoryDiv) {
    const category = categoryDiv ? categoryDiv.dataset.category : "";

    let html = "";
    if (category.toLowerCase() === "person") {
        const photoButton = item.profile_path
            ? `<a href="https://image.tmdb.org/t/p/w500${item.profile_path}" target="_blank" class="btn btn-outline-primary btn-sm float-end ms-3">Photo</a>`
            : "";

        html = `
            <div class="card mb-3 shadow-sm p-3">
                <div class="card-body">
                    ${photoButton}
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="card-title mb-0"><strong>${item.name || "Unknown"}</strong></h5>
                        <a href="/person/${item.id}/stats" class="btn btn-outline-dark btn-sm">Known for</a>
                    </div>
                    <p class="card-text">
                        <strong>ID:</strong> ${item.id}<br>
                        <strong>Popularity:</strong> ${item.popularity ?? "N/A"}<br>
                        <strong>Gender:</strong> ${item.gender === 1 ? "F" : item.gender === 2 ? "M" : "Other"}<br>
                        <strong>Department:</strong> ${item.known_for_department || "N/A"}
                    </p>
                </div>
            </div>
        `;
        categoryDiv.insertAdjacentHTML("beforeend", html);
        return;
    }

    // existing logic for movie/tv
    const title = item.title || item.name || "Unknown";
    const id = item.id;
    const language = item.original_language || "N/A";
    const releaseDate = item.release_date || item.first_air_date || "N/A";
    const popularity = item.popularity ?? "N/A";
    const vote = item.vote_average ?? "N/A";

    let genres = "N/A";
    if (item.genre_names && item.genre_names.length > 0) {
        genres = item.genre_names.join(", ");
    }

    let financialButton = "";
    if (category === "movie") {
        financialButton = `
            <a href="/flicklytics/financial-performance/${id}" class="btn btn-outline-success btn-sm">
                Financial Performance
            </a>
        `;
    }

    html = `
        <div class="card mb-3 shadow-sm p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <h5 class="card-title mb-0">
                        <a href="/flicklytics/${category}/${id}">${title}</a>
                    </h5>
                    <div class="d-flex gap-2">
                        <a href="/flicklytics/global-diversity/${category}/${id}" class="btn btn-outline-primary btn-sm">Global Diversity</a>
                        <a href="/flicklytics/reviews/${category}/${id}" class="btn btn-outline-secondary btn-sm">Reviews</a>
                        ${financialButton}
                    </div>
                </div>
                <p class="card-text">
                    <strong>ID:</strong> ${id}<br>
                    <strong>Language:</strong> ${language}<br>
                    <strong>Genres:</strong> ${genres}<br>
                    <strong>Release Date:</strong> ${releaseDate}<br>
                    <strong>Popularity:</strong> ${popularity}<br>
                    <strong>Vote Average:</strong> ${vote}
                </p>
            </div>
        </div>
    `;

    categoryDiv.insertAdjacentHTML("beforeend", html);
}

// Send a search query + category as JSON to the server
function search(query, category) {
    const resultsContainer = document.querySelector("#results");

    // Create a new header div for this search
    const header = document.createElement("div");
    header.classList.add("search-block");

    header.innerHTML = `
        <div class="mb-3 mt-4">
            <h4>
                Search terms: ${query}.
                Category: ${category}.
            </h4>
        </div>
        <div class="category-results" data-category="${category}"></div>
    `;

    // Insert the new search block at the top of the results container
    resultsContainer.prepend(header);
    latestCategoryDiv = header.querySelector(".category-results");

    // Send the search to the server
    ws.send(JSON.stringify({
        query: query,
        category: category
    }));
}

document.addEventListener("DOMContentLoaded", () => {
    initWebSocket();

    document.querySelector("#search_form").addEventListener("submit", (e) => {
        e.preventDefault();

        const query = document.querySelector("#queryInput").value.trim();
        const category = document.querySelector("#categoryInput").value.trim();

        if (!query) {
            alert("Please enter a search query.");
            return;
        }

        if (!category) {
            alert("Please select a category.");
            return;
        }

        // Send the search
        search(query, category);
    });
});