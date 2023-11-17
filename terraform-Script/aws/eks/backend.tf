terraform {
  backend "s3" {
    bucket         = "opsmx-terraform-state"
    key            = "${var.project}-state.tfstate"
    region         = "us-west-2"
    encrypt        = true
  }
}
