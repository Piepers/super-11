<template>
    <div id="app">
        <Standings />
    </div>
</template>

<script>
    import Standings from './components/Standings.vue'

    export default {
        name: 'app',
        components: {
            Standings
        }
    }
</script>

<style>
    body {
        font-family: sans-serif;
        font-size: 0.9em;
        line-hight: 1.5em;
        background-image: url(assets/background.jpg);
        background-size: cover;
        background-attachment: fixed;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
    }

    /*#app {*/

    /*}*/
</style>
