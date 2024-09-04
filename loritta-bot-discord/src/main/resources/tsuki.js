// Tsuki: A small helper-like library inspired by Surreal.js
function tsukify(element) {
    // Null, bail out!
    if (element === null)
        return element

    // Already tsukified, bail out!
    if (element.isTsukified)
        return element

    element.on = (type, func) => { element.addEventListener(type, func); return element; }
    element.hasClass = (clazz) => { return element.classList.contains(clazz); }
    element.addClass = (clazz) => { element.classList.add(clazz); return element; }
    element.removeClass = (clazz) => { element.classList.remove(clazz); return element; }
    element.toggleClass = (clazz) => {
        console.log(element)
        if (element.hasClass(clazz)) {
            element.removeClass(clazz);
        } else {
            element.addClass(clazz);
        }
        return element;
    }

    element.whenRemovedFromDOM = (func) => {
        // Create a MutationObserver to watch for the div being removed from the DOM
        const observer = new MutationObserver((mutations) => {
            // console.log("mutation happened")
            if (!document.contains(self)) {
                // Div is removed, remove the listener
                // console.log("bye!!!")
                func()
                observer.disconnect() // Stop observing once done
            }
        });

        // Start observing the parent of the div for childList changes (additions/removals)
        console.log(self.parentNode)
        observer.observe(document.body, { childList: true, subtree: true });

        return element;
    }

    element.isTsukified = true

    return element
}

// Gets the parent element of the <script>
function me() {
    const scriptElement = document.currentScript;
    return tsukify(scriptElement.parentNode);
}

// Selects the first that matches selector
function selectFirst(selector) {
    const result = document.querySelector(selector)
    if (result === null)
        return
    return tsukify(result)
}