# Dora

![https://upload.wikimedia.org/wikipedia/en/9/99/Dora_the_Explorer_%28character%29.webp](https://upload.wikimedia.org/wikipedia/en/9/99/Dora_the_Explorer_%28character%29.webp)

A simple i18n web tool used to translate Loritta into other languages.

It translates everything using LLMs as the "base", which then human reviewers can edit any mistranslations by the machine.

While having humans review ALL translations would be ideal, it would be STUPIDLY time-consuming for large projects like Loritta. (trust me, we tried)

So it is better to rely on LLMs to do the heavy lifting, and have humans edit only the mistranslations.

## Why not use Crowdin?

* Crowdin is expensive for our projects. It would be, at least, 50 USD monthly! And that's too dang expensive for a tool like this.
* We can integrate better with Loritta's requirements and code.
* I love reinventing the wheel! :3