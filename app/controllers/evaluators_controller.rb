class EvaluatorsController < ApplicationController
  def show
    data = {:evaluators => [{:id => "ev_0"}, {:id => "ev_1"}, {:id => "ev_2"}, {:id => "ev_3"}, {:id => "ev_4"}, ]}
    render :json => data
  end

  def status
    data = {:evaluator_id => params[:id], :status => "AVAILABLE", :assigned_task_id => "task_"+params[:id]}
    render :json => data
  end

  def metric
    data = {:metric => params[:metric], :points => get_data}
    render :json => data
  end

  private
  def get_data
    data = Array.new
    time = js_current_time
    rand = Random.new(Random.new_seed)

    i = -19

    while i <= 0 do
      data.push(
        {
          :time => time + i*1000,
          :value => rand.rand
        }
      )
      i = i+1
    end
    return data
  end

  def js_current_time
    time = Time.new.getlocal
    time = time.to_i * 1000
    return time
  end
end
