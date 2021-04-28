import twitterEmoji from 'twemoji'
import type { ParseObject } from 'twemoji';
import { afterUpdate } from 'svelte'

type TwemojiHow = Partial<ParseObject>;

const defaults: TwemojiHow = {
    folder: 'svg',
    ext: '.svg'
}

export default function twemoji(node: HTMLElement, how: TwemojiHow = {}) {
    how = { ...defaults, ...how };
    twitterEmoji.parse(node, how)
    afterUpdate(() => {
        twitterEmoji.parse(node, how)
    })
    return {
        update() {
            console.warn('Changing twemoji options after the action was mounted is not possible.')
        }
    }
}
