// Tsuki: A small helper-like library inspired by Surreal.js
class TsukiElement {
    /**
     * Creates an instance of TsukiElement
     * @param {Element} handle - The DOM element to wrap.
     */
    constructor(handle) {
        this.handle = handle;
    }

    on(type, func) {
        this.handle.addEventListener(type, func);
        return this;
    }

    addClass(clazz) {
        this.handle.classList.add(clazz);
        return this;
    }

    hasClass(clazz) {
        return this.handle.classList.contains(clazz);
    }

    removeClass(clazz) {
        this.handle.classList.remove(clazz);
        return this;
    }

    toggleClass(clazz) {
        if (this.hasClass(clazz)) {
            this.removeClass(clazz);
        } else {
            this.addClass(clazz);
        }
        return this;
    }

    remove() {
        this.handle.remove()
        return this;
    }

    whenRemovedFromDOM(func) {
        // Create a MutationObserver to watch for the div being removed from the DOM
        const observer = new MutationObserver((mutations) => {
            // console.log("mutation happened")
            if (!document.contains(this.handle)) {
                // Div is removed, remove the listener
                // console.log("bye!!!")
                func()
                observer.disconnect() // Stop observing once done
            }
        });

        // Start observing the parent of the div for childList changes (additions/removals)
        console.log(self.parentNode)
        observer.observe(document.body, { childList: true, subtree: true });

        return this;
    }

    selectFirst(selector) {
        const result = this.handle.querySelector(selector)
        if (result === null)
            return
        return tsukify(result)
    }

    closest(selector) {
        const result = this.handle.closest(selector)
        if (result === null)
            return
        return tsukify(result)
    }
}

/**
 * Tsukifies a element
 * @returns {TsukiElement|null}
 */
function tsukify(element) {
    // Null, bail out!
    if (element === null)
        return element

    // Already tsukified, bail out!
    if (element instanceof TsukiElement)
        return element

    return new TsukiElement(element)
}

/**
 * Gets the parent element of the <script>
 * @returns {TsukiElement}
 */
function me() {
    const scriptElement = document.currentScript;
    return tsukify(scriptElement.parentNode);
}

/**
 * Gets the parent element of the <script>
 * @returns {TsukiElement|null}
 */
function selectFirst(selector) {
    const result = document.querySelector(selector)
    if (result === null)
        return null
    return tsukify(result)
}