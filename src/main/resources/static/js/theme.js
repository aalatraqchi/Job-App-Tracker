(() => {
    const storageKey = "job-app-tracker-theme";
    const themes = new Set(["dark", "light", "ocean", "forest", "sunset"]);
    let theme = "dark";

    try {
        const savedTheme = localStorage.getItem(storageKey);
        if (themes.has(savedTheme)) {
            theme = savedTheme;
        }
    } catch {
        // The page still works when browser storage is unavailable.
    }

    // Apply the preference before the stylesheet paints the page.
    document.documentElement.dataset.theme = theme;

    document.addEventListener("DOMContentLoaded", () => {
        const selector = document.getElementById("theme-select");
        if (!selector) {
            return;
        }

        selector.value = theme;
        selector.closest(".theme-picker").hidden = false;
        selector.addEventListener("change", () => {
            if (!themes.has(selector.value)) {
                return;
            }

            document.documentElement.dataset.theme = selector.value;
            try {
                localStorage.setItem(storageKey, selector.value);
            } catch {
                // Allow switching for this page even without saved preferences.
            }
        });
    });
})();
