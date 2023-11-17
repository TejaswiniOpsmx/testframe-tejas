
data "aws_eks_cluster_auth" "ephemeral" {
  name = "${var.project}-cluster"	
}

locals {
  kubeconfig = <<KUBECONFIG

apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: ${aws_eks_cluster.this.certificate_authority.0.data}
    server: ${aws_eks_cluster.this.endpoint}
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    user: aws
  name: aws
current-context: aws
kind: Config
preferences: {}
users:
- name: aws
  user:
    exec:
      apiVersion: client.authentication.k8s.io/v1beta1
      args:
      - --region
      - ${var.region}
      - eks
      - get-token
      - --cluster-name
      - ${aws_eks_cluster.this.name}
      command: aws    
    
    
KUBECONFIG
}


resource "local_file" "kubefile" {
  content  = "${local.kubeconfig}"
  filename = "${path.module}/kubefile"
}
