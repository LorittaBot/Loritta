/**
 * Tsukifies a element
 */
function tsukify(element) {
    // Null, bail out!
    if (element === null)
        return element

    // Already tsukified, bail out!
    if (element.isTsukified === true)
        return element

    element.on = (type, func) => {
        this.addEventListener(type, func);
        return this;
    }

    element.addClass = (clazz) => {
        this.classList.add(clazz);
        return this;
    }

    element.hasClass = (clazz) => {
        return this.classList.contains(clazz);
    }

    element.removeClass = (clazz) => {
        this.classList.remove(clazz);
        return this;
    }

    element.toggleClass = (clazz) => {
        if (this.hasClass(clazz)) {
            this.removeClass(clazz);
        } else {
            this.addClass(clazz);
        }
        return this;
    }

    element.remove = () => {
        this.remove()
        return this;
    }

    element.whenRemovedFromDOM = (func) => {
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

    element.selectFirst = (selector) => {
        const result = this.querySelector(selector)
        if (result === null)
            return
        return tsukify(result)
    }

    element.closest = (selector) => {
        const result = this.closest(selector)
        if (result === null)
            return
        return tsukify(result)
    }

    element.isTsukified = true;

    return element;
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
 * Wraps document.querySelector
 * @returns {TsukiElement|null}
 */
function selectFirst(selector) {
    const result = document.querySelector(selector)
    if (result === null)
        return null
    return tsukify(result)
}