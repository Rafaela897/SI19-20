(deftemplate vehicle
    (slot water)
    (slot fuel)
    (slot maxFuel)
    (slot maxWater)
    )
 
 (deftemplate decision
 	(slot value)
 )
 

   
(defrule allocate_time
	?v <- (vehicle (fuel ?f) (water ?w) (maxFuel ?f1) (maxWater ?w1) )
	=>
	(if (and (<= ?w  2) (> ?w1 2 )) then  (assert (decision (value 0) ))
	else (if (<= ?f (- ?f1 1 ) ) then (assert (decision (value 1))) else 
	(if (< ?w ?w1) then (assert (decision (value 0))) else
	(assert (decision (value 3))))))
	)