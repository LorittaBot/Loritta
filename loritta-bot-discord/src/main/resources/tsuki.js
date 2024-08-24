// Tsuki: A small helper-like library inspired by Surreal.js
function tsukify(element) {
    // Null, bail out!
    if (element === null)
        return element

    // Already tsukified, bail out!
    if (element.isTsukified)
        return element

    element.on = (type, func) => { element.addEventListener(type, func); return element; }
    element.addClass = (clazz) => { element.classList.add(clazz); return element; }
    element.removeClass = (clazz) => { element.classList.remove(clazz); return element; }

    element.isTsukified = true

    return element
}

// Gets the parent element of the <script>
function me() {
    const scriptElement = document.currentScript;
    return tsukify(scriptElement.parentNode);
}