# Kubernetes deployment

`app.yml` deploys only the Asteral application. It deliberately does not pretend
to provision a production PostgreSQL database, ingress, TLS, backups, or secret
manager.

Before applying:

1. Replace `postgres.example.internal` and `asteral:local`.
2. Create the required secret without committing values:

```bash
kubectl create secret generic asteral-secrets \
  --from-literal=DB_PASSWORD='replace-with-secret-manager-value' \
  --from-literal=NASA_API_KEY='replace-with-nasa-key'
```

Validate and apply:

```bash
kubectl apply --dry-run=client -f k8s/app.yml
kubectl apply -f k8s/app.yml
```
