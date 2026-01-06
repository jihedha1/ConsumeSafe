// src/main/resources/static/js/app.js

/**
 * ConsumeSafe - Application JavaScript Globale
 * Gestion du thème, favoris, partage social, animations
 */

// ==================== THEME MANAGER ====================
class ThemeManager {
    constructor() {
        this.currentTheme = localStorage.getItem('theme') || 'dark';
        this.init();
    }

    init() {
        document.body.setAttribute('data-theme', this.currentTheme);
        this.updateThemeUI();
        this.attachListeners();
    }

    toggle() {
        this.currentTheme = this.currentTheme === 'dark' ? 'light' : 'dark';
        document.body.setAttribute('data-theme', this.currentTheme);
        localStorage.setItem('theme', this.currentTheme);
        this.updateThemeUI();

        // Effet de transition fluide
        document.body.style.transition = 'all 0.5s ease';
        setTimeout(() => {
            document.body.style.transition = '';
        }, 500);
    }

    updateThemeUI() {
        const icon = document.getElementById('themeIcon');
        const text = document.getElementById('themeText');

        if (icon && text) {
            if (this.currentTheme === 'dark') {
                icon.textContent = '🌙';
                text.textContent = 'Sombre';
            } else {
                icon.textContent = '☀️';
                text.textContent = 'Clair';
            }
        }
    }

    attachListeners() {
        const toggle = document.getElementById('themeToggle');
        if (toggle) {
            toggle.addEventListener('click', () => this.toggle());
        }
    }
}

// ==================== FAVORITES MANAGER ====================
class FavoritesManager {
    constructor() {
        this.favorites = this.load();
    }

    load() {
        return JSON.parse(localStorage.getItem('favorites') || '[]');
    }

    save() {
        localStorage.setItem('favorites', JSON.stringify(this.favorites));
    }

    add(item) {
        if (!this.favorites.includes(item)) {
            this.favorites.push(item);
            this.save();
            return true;
        }
        return false;
    }

    remove(item) {
        const index = this.favorites.indexOf(item);
        if (index > -1) {
            this.favorites.splice(index, 1);
            this.save();
            return true;
        }
        return false;
    }

    toggle(item) {
        if (this.has(item)) {
            this.remove(item);
            return false;
        } else {
            this.add(item);
            return true;
        }
    }

    has(item) {
        return this.favorites.includes(item);
    }

    getAll() {
        return [...this.favorites];
    }

    clear() {
        this.favorites = [];
        this.save();
    }

    export() {
        const dataStr = JSON.stringify(this.favorites, null, 2);
        const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);

        const exportFileDefaultName = `consumesafe-favoris-${new Date().toISOString().slice(0,10)}.json`;

        const linkElement = document.createElement('a');
        linkElement.setAttribute('href', dataUri);
        linkElement.setAttribute('download', exportFileDefaultName);
        linkElement.click();
    }
}

// ==================== SEARCH MANAGER ====================
class SearchManager {
    constructor(inputId, suggestionsId) {
        this.input = document.getElementById(inputId);
        this.suggestionsDiv = document.getElementById(suggestionsId);
        this.debounceTimer = null;
        this.init();
    }

    init() {
        if (!this.input || !this.suggestionsDiv) return;

        this.input.addEventListener('input', (e) => this.handleInput(e));
        this.suggestionsDiv.addEventListener('click', (e) => this.handleSuggestionClick(e));
        document.addEventListener('click', (e) => this.handleOutsideClick(e));
    }

    handleInput(e) {
        clearTimeout(this.debounceTimer);
        const query = e.target.value.trim();

        if (query.length < 2) {
            this.hideSuggestions();
            return;
        }

        this.debounceTimer = setTimeout(() => {
            this.fetchSuggestions(query);
        }, 300);
    }

    async fetchSuggestions(query) {
        try {
            const response = await fetch(`/api/suggestions?query=${encodeURIComponent(query)}`);
            const suggestions = await response.json();

            if (suggestions.length > 0) {
                this.displaySuggestions(suggestions);
            } else {
                this.hideSuggestions();
            }
        } catch (error) {
            console.error('Erreur lors de la récupération des suggestions:', error);
            this.hideSuggestions();
        }
    }

    displaySuggestions(suggestions) {
        this.suggestionsDiv.innerHTML = suggestions.map(s =>
            `<div class="suggestion-item" data-value="${this.escapeHtml(s)}">${this.escapeHtml(s)}</div>`
        ).join('');
        this.suggestionsDiv.classList.add('show');
    }

    hideSuggestions() {
        this.suggestionsDiv.classList.remove('show');
    }

    handleSuggestionClick(e) {
        if (e.target.classList.contains('suggestion-item')) {
            this.input.value = e.target.dataset.value;
            this.hideSuggestions();
            this.input.form.submit();
        }
    }

    handleOutsideClick(e) {
        if (!e.target.closest('.input-group')) {
            this.hideSuggestions();
        }
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// ==================== SHARE MANAGER ====================
class ShareManager {
    static shareOnFacebook(url, text) {
        const shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}&quote=${encodeURIComponent(text)}`;
        window.open(shareUrl, '_blank', 'width=600,height=400');
    }

    static shareOnTwitter(url, text) {
        const shareUrl = `https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(text)}`;
        window.open(shareUrl, '_blank', 'width=600,height=400');
    }

    static shareOnWhatsApp(text) {
        const shareUrl = `https://wa.me/?text=${encodeURIComponent(text)}`;
        window.open(shareUrl, '_blank');
    }

    static async copyToClipboard(text) {
        try {
            await navigator.clipboard.writeText(text);
            return true;
        } catch (err) {
            console.error('Erreur lors de la copie:', err);
            return false;
        }
    }
}

// ==================== TOAST NOTIFICATIONS ====================
class ToastNotification {
    static show(message, duration = 3000, type = 'success') {
        const toast = document.getElementById('toast') || this.createToast();

        toast.textContent = message;
        toast.className = `toast ${type}`;
        toast.classList.add('show');

        setTimeout(() => {
            toast.classList.remove('show');
        }, duration);
    }

    static createToast() {
        const toast = document.createElement('div');
        toast.id = 'toast';
        toast.className = 'toast';
        document.body.appendChild(toast);
        return toast;
    }
}

// ==================== SCROLL ANIMATIONS ====================
class ScrollAnimations {
    constructor() {
        this.elements = document.querySelectorAll('.reveal');
        this.init();
    }

    init() {
        this.checkScroll();
        window.addEventListener('scroll', () => this.checkScroll());
    }

    checkScroll() {
        this.elements.forEach(element => {
            const elementTop = element.getBoundingClientRect().top;
            const windowHeight = window.innerHeight;

            if (elementTop < windowHeight - 100) {
                element.classList.add('active');
            }
        });
    }
}

// ==================== LAZY LOADING IMAGES ====================
class LazyLoader {
    constructor() {
        this.images = document.querySelectorAll('img[data-src]');
        this.init();
    }

    init() {
        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        img.src = img.dataset.src;
                        img.classList.add('loaded');
                        observer.unobserve(img);
                    }
                });
            });

            this.images.forEach(img => imageObserver.observe(img));
        } else {
            // Fallback pour navigateurs anciens
            this.images.forEach(img => {
                img.src = img.dataset.src;
            });
        }
    }
}

// ==================== PERFORMANCE MONITOR ====================
class PerformanceMonitor {
    static logPageLoad() {
        window.addEventListener('load', () => {
            const perfData = window.performance.timing;
            const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart;
            console.log(`📊 Temps de chargement: ${pageLoadTime}ms`);
        });
    }
}

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', () => {
    // Initialiser le gestionnaire de thème
    window.themeManager = new ThemeManager();

    // Initialiser le gestionnaire de favoris
    window.favoritesManager = new FavoritesManager();

    // Initialiser la recherche si présente
    if (document.getElementById('productInput')) {
        window.searchManager = new SearchManager('productInput', 'suggestions');
    }

    // Initialiser les animations au scroll
    if (document.querySelectorAll('.reveal').length > 0) {
        new ScrollAnimations();
    }

    // Initialiser le lazy loading
    if (document.querySelectorAll('img[data-src]').length > 0) {
        new LazyLoader();
    }

    // Moniteur de performance (dev uniquement)
    if (window.location.hostname === 'localhost') {
        PerformanceMonitor.logPageLoad();
    }

    // Masquer le loader
    const loader = document.getElementById('loader');
    if (loader) {
        setTimeout(() => {
            loader.classList.add('hidden');
        }, 1000);
    }
});

// ==================== EXPORT GLOBAL ====================
window.ConsumeSafe = {
    ThemeManager,
    FavoritesManager,
    SearchManager,
    ShareManager,
    ToastNotification,
    ScrollAnimations,
    LazyLoader,
    PerformanceMonitor
};