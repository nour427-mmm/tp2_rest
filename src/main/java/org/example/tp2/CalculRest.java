package org.example.tp2;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/calcul")
public class CalculRest {

    private final Calculatrice calculService = new Calculatrice();


    @GET
    @Path("/somme")
    @Produces(MediaType.TEXT_PLAIN)
    public Response somme(@QueryParam("a") int a, @QueryParam("b") int b) {
        int res = calculService.somme(a, b);
        return Response.ok(res).build();
    }


    @GET
    @Path("/soustraction")
    @Produces(MediaType.TEXT_PLAIN)
    public Response soustraction(@QueryParam("a") int a, @QueryParam("b") int b) {
        int res = calculService.soustraction(a, b);
        return Response.ok(res).build();
    }


    @GET
    @Path("/produit")
    @Produces(MediaType.TEXT_PLAIN)
    public Response produit(@QueryParam("a") int a, @QueryParam("b") int b) {
        int res = calculService.produit(a, b);
        return Response.ok(res).build();
    }

    // 4. Division avec gestion d'erreur : /calcul/division?a=10&b=2
    @GET
    @Path("/division")
    @Produces(MediaType.TEXT_PLAIN)
    public Response division(@QueryParam("a") int a, @QueryParam("b") int b) {
        if (b == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur : Division par zero impossible")
                    .build();
        }
        double res = calculService.division(a, b);
        return Response.ok(res).build();
    }


    @GET
    @Path("/produit/{a}/{b}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response produitChemin(@PathParam("a") int a, @PathParam("b") int b) {
        int res = calculService.produit(a, b);
        return Response.ok(res).build();
    }
    // 6. Somme détaillée en JSON : /calcul/somme/detail?a=2&b=3
    @GET
    @Path("/somme/detail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sommeDetail(@QueryParam("a") int a, @QueryParam("b") int b) {
        Resultat res = new Resultat("somme", a, b, calculService.somme(a, b));
        return Response.ok(res).build();
    }
}