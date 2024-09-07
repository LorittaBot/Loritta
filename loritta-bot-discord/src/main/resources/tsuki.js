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
        element.addEventListener(type, func);
        return element;
    }

    element.addClass = (clazz) => {
        element.classList.add(clazz);
        return element;
    }

    element.hasClass = (clazz) => {
        return element.classList.contains(clazz);
    }

    element.removeClass = (clazz) => {
        element.classList.remove(clazz);
        return element;
    }

    element.toggleClass = (clazz) => {
        if (element.hasClass(clazz)) {
            element.removeClass(clazz);
        } else {
            element.addClass(clazz);
        }
        return element;
    }

    const originalRemove = element.remove
    element.remove = () => {
        originalRemove.call(element)
        return this;
    }

    element.whenRemovedFromDOM = (func) => {
        // Create a MutationObserver to watch for the div being removed from the DOM
        const observer = new MutationObserver((mutations) => {
            // console.log("mutation happened")
            if (!document.contains(element)) {
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
        const result = element.querySelector(selector)
        if (result === null)
            return
        return tsukify(result)
    }

    const originalClosest = element.closest
    element.closest = (selector) => {
        const result = originalClosest.call(element, selector)
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