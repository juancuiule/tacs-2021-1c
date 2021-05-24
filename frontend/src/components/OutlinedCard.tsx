import Button from "@material-ui/core/Button";
import Card from "@material-ui/core/Card";
import CardActions from "@material-ui/core/CardActions";
import CardContent from "@material-ui/core/CardContent";
import Typography from "@material-ui/core/Typography";
import React from "react";

interface Props {
  title: string;
  description: string;
  actionText: string;
  onClick: () => void;
}

export default function OutlinedCard(props: Props) {
  const { title, description, actionText, onClick } = props;

  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h5" component="h2">
          {title}
        </Typography>
        <Typography variant="body2" component="p">
          {description}
        </Typography>
      </CardContent>
      <CardActions>
        <Button size="small" fullWidth onClick={onClick}>
          {actionText}
        </Button>
      </CardActions>
    </Card>
  );
}
