<template>
    <div class="hello">
        <!--{{name}}-->
        <ul>
        <li v-for="draft in drafts" v-bind:key="draft.id">{{draft.managerName}}</li>
        </ul>
        <!--<button v-on:click="changeName" v-bind:disabled="btnState">Change name</button>-->
    </div>
</template>

<script>
    export default {
        name: 'Standings',
        data() {
            return {
                drafts:[]
            }
        },
        mounted() {
            fetch("http://localhost:8080/api/standings")
                .then(response => response.json())
                .then((data) => {
                    this.drafts = data.data.drafts
                })
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    h3 {
        margin: 40px 0 0;
    }

    ul {
        list-style-type: none;
        padding: 0;
    }

    li {
        display: inline-block;
        margin: 0 10px;
    }

    a {
        color: #42b983;
    }
</style>
