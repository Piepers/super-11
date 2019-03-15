<template>
    <div>
        <article>
            <table align="center">
                <thead>
                <tr>
                    <td colspan="4" class="header">
                        <h1>Super 11 Uden Standings</h1>
                    </td>
                </tr>
                <tr>
                    <th>Pos</th>
                    <th>Team</th>
                    <th>R</th>
                    <th>Pts</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="draft in drafts">
                    <td>
                        {{draft.rank}}
                    </td>
                    <td>
                        {{draft.draftName}}
                    </td>
                    <td>
                        {{draft.points}}
                    </td>
                    <td>
                        {{draft.totalPoints}}
                    </td>
                </tr>
                </tbody>
            </table>
        </article>
    </div>
</template>

<script>
    export default {
        name: 'Standings',
        data() {
            return {
                drafts: []
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
    table {
        border: 1px solid black;
        border-collapse: collapse;
        opacity: 0.95;
        width: 85%;
    }

    th {
        background: #a70000;
        color: white;
    }

    tr:nth-child(even) {
        background: #e8e8e8;
    }

    tr:nth-child(odd) {
        background: white;
    }

    .header {
        background: #005cb7;
        color: white;
    }

    th, td {
        text-align: center;
        padding: 10px;
    }

    article {
        overflow-x: auto;
    }
</style>
