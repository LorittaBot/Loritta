@file:JsModule("fs")
@file:JsNonModule

import nodecanvas.Buffer

external fun writeFile(name: String, buffer: Buffer, callback: dynamic)