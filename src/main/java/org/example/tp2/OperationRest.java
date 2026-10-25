package org.example.tp2;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OperationRest {


    private static final Map<Integer, Operation> OPERATIONS = new ConcurrentHashMap<>();
    private static final AtomicInteger COMPTEUR = new AtomicInteger(0);
    private final Calculatrice calculService = new Calculatrice();


    private boolean calculer(Operation op) {
        if (op == null || op.getOperateur() == null) return false;
        switch (op.getOperateur().toLowerCase()) {
            case "somme":
                op.setResultat(calculService.somme(op.getA(), op.getB()));
                return true;
            case "soustraction":
                op.setResultat(calculService.soustraction(op.getA(), op.getB()));
                return true;
            case "produit":
                op.setResultat(calculService.produit(op.getA(), op.getB()));
                return true;
            case "division":
                if (op.getB() == 0) return false;
                op.setResultat(calculService.division(op.getA(), op.getB()));
                return true;
            default:
                return false;
        }
    }


    @POST
    public Response creer(Operation op, @Context UriInfo uriInfo) {
        if (!calculer(op)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"erreur\":\"Operateur invalide ou division par zero\"}")
                    .build();
        }
        int id = COMPTEUR.incrementAndGet();
        op.setId(id);
        OPERATIONS.put(id, op);

        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(location).entity(op).build();
    }


    @GET
    public List<Operation> lister() {
        return new ArrayList<>(OPERATIONS.values());
    }


    @GET
    @Path("/{id}")
    public Response lire(@PathParam("id") int id) {
        Operation op = OPERATIONS.get(id);
        if (op == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(op).build();
    }


    @PUT
    @Path("/{id}")
    public Response modifier(@PathParam("id") int id, Operation op) {
        if (!OPERATIONS.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (!calculer(op)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"erreur\":\"Calcul impossible\"}")
                    .build();
        }
        op.setId(id);
        OPERATIONS.put(id, op);
        return Response.ok(op).build();
    }


    @DELETE
    @Path("/{id}")
    public Response supprimer(@PathParam("id") int id) {
        Operation removed = OPERATIONS.remove(id);
        if (removed == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build(); // Status 204 No Content
    }
}