<script lang="ts">
    import twemoji from "./Twemoji";
</script>

<div use:twemoji class="loader">🕛</div>

<style>
    .loader {
        display: inline-block;
        width: 36px;
        height: 36px;
        animation: spin 1s infinite linear;
        line-height: 0;
        font-size: 36px;
    }

    @keyframes spin {
        from {
            -moz-transform: rotate(0deg);
            -ms-transform: rotate(0deg);
            -o-transform: rotate(0deg);
            -webkit-transform: rotate(0deg);
            transform: rotate(0deg);
        }

        to {
            -moz-transform: rotate(360deg);
            -ms-transform: rotate(360deg);
            -o-transform: rotate(360deg);
            -webkit-transform: rotate(360deg);
            transform: rotate(360deg);
        }
    }
</style>
